package io.sourcesync.android;

import android.content.Context;
import android.view.View;
import android.widget.VideoView;
import android.os.Handler;
import android.widget.FrameLayout;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import android.view.ViewGroup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sourcesync.android.activation.Activation;
import io.sourcesync.android.activation.ActivationStateListener;

public class SourceSync implements ActivationStateListener {
    private static final String TAG = "SourceSync.main";
    private static final String CONFIG_FILE = "sourcesync_config.json";
    private static final String CONFIG_FILE_SCHEMA = "sourcesync_config_schema.json";

    private final Context context;
    private final String apiKey;
    private final JSONObject settings;
    private final Map<String, ViewGroup> positionContainers = new HashMap<>();
    private final Map<Integer, Activation> activations = new HashMap<>();
    private final Map<String, Integer> activePositionActivations = new HashMap<>();
    private final ApiClient apiClient;
    private static JsonSchema validationSchema;
    private Handler playbackHandler;
    private Runnable playbackRunnable;
    private Distribution currentDistribution;
    private Integer activeDetailActivation;
    private boolean isInSeek = false;
    private int lastProcessedPosition = -1;

    private SourceSync(Context context, String apiKey, JSONObject settings) {
        this.context = context.getApplicationContext();
        this.apiKey = apiKey;
        this.settings = settings;
        this.playbackHandler = new Handler();
        this.apiClient = new ApiClient(apiKey);
    }

    public static SourceSync setup(Context context, String apiKey) {
        return setup(context, apiKey, "{}");
    }

    public static SourceSync setup(Context context, String apiKey, String customSettings) {
        Log.d(TAG, "Starting SourceSync setup with apiKey: " + apiKey);

        try {
            // Load and cache schema
            if (validationSchema == null) {
                Log.d(TAG, "Loading JSON schema from " + CONFIG_FILE_SCHEMA);
                String schemaJson = loadResourceFile(context, CONFIG_FILE_SCHEMA);
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                validationSchema = factory.getSchema(schemaJson);
                Log.d(TAG, "Schema loaded and cached successfully");
            }

            // Load base config
            Log.d(TAG, "Loading default configuration from " + CONFIG_FILE);
            String defaultConfigStr = loadResourceFile(context, CONFIG_FILE);
            JSONObject defaultConfig = new JSONObject(defaultConfigStr);
            Log.d(TAG, "Default configuration loaded: " + defaultConfig.toString());

            // Get API config
            ApiClient setupClient = new ApiClient(apiKey);
            JSONObject apiConfig = setupClient.getSettings();

            // Merge configurations
            Log.d(TAG, "Starting configuration merge process");
            Log.d(TAG, "Merging default config with API config");
            JSONObject mergedWithApi = mergeJSONObjects(defaultConfig, apiConfig);
            Log.d(TAG, "Config after API merge: " + mergedWithApi.toString());

            Log.d(TAG, "Parsing custom settings: " + customSettings);
            JSONObject customConfig = new JSONObject(customSettings);

            Log.d(TAG, "Merging with custom settings");
            JSONObject finalConfig = mergeJSONObjects(mergedWithApi, customConfig);
            Log.d(TAG, "Final merged configuration: " + finalConfig.toString());

            // Validate merged configuration
            Log.d(TAG, "Starting configuration validation");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(finalConfig.toString());

            Set<ValidationMessage> validationResult = validationSchema.validate(jsonNode);
            if (!validationResult.isEmpty()) {
                Log.e(TAG, "Configuration validation failed: " + validationResult.toString());
                throw new IllegalArgumentException("Invalid configuration: " + validationResult.toString());
            }
            Log.d(TAG, "Configuration validation successful");

            Log.d(TAG, "SourceSync setup completed successfully");
            return new SourceSync(context, apiKey, finalConfig);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SourceSync", e);
            throw new IllegalArgumentException("Failed to initialize SourceSync", e);
        }
    }

    private static String loadResourceFile(Context context, String filename) throws IOException {
        try (InputStream is = context.getAssets().open(filename)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }

    private static JSONObject mergeJSONObjects(JSONObject defaults, JSONObject custom)
            throws JSONException {
        JSONObject merged = new JSONObject(defaults.toString()); // Deep copy

        Iterator<String> keys = custom.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = custom.get(key);
            if (value instanceof JSONObject) {
                // Recursively merge nested objects
                if (merged.has(key)) {
                    merged.put(key, mergeJSONObjects(
                        merged.getJSONObject(key),
                        custom.getJSONObject(key)
                    ));
                } else {
                    merged.put(key, value);
                }
            } else {
                // For non-objects, custom value completely replaces default
                merged.put(key, value);
            }
        }

        return merged;
    }

    public Distribution getDistribution(String id) throws IOException, JSONException {
        JSONObject response = apiClient.getDistribution(id);
        currentDistribution = Distribution.fromJson(response);
        preloadActivations();
        return currentDistribution;
    }

    private void preloadActivations() {
        for (Distribution.ActivationInstance instance : currentDistribution.activations) {
            try {
                loadActivation(String.valueOf(instance.externalId));
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Failed to preload activation " + instance.externalId, e);
            }
        }
    }

    private Activation loadActivation(String id) throws IOException, JSONException {
        int activationId = Integer.parseInt(id);

        if (activations.containsKey(activationId)) {
            return activations.get(activationId);
        }

        JSONObject response = apiClient.getActivation(id);
        Activation activation = Activation.fromJson(response);
        activation.initialize(context, this, this.settings);
        activations.put(activationId, activation);
        return activation;
    }

    public Map<String, View> createPositionedOverlays(Distribution distribution, VideoView videoView, String... positions) {
        Map<String, View> containers = new HashMap<>();

        for (String position : positions) {
            FrameLayout container = new FrameLayout(context);
            container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ));

            positionContainers.put(position, container);
            containers.put(position, container);
            activePositionActivations.put(position, -1);
        }

        setupVideoPlaybackMonitoring(videoView);
        return containers;
    }

    private void setupVideoPlaybackMonitoring(VideoView videoView) {
        videoView.setOnPreparedListener(mp -> {
            Log.d(TAG, "Video prepared, starting playback monitoring");
            mp.setOnSeekCompleteListener(mediaPlayer -> {
                int currentPosition = videoView.getCurrentPosition();
                Log.d(TAG, "Seek completed to position: " + currentPosition);
                isInSeek = false;
                lastProcessedPosition = -1;
                updateActivations(currentPosition);
                startPlaybackMonitoring(videoView);
            });
            startPlaybackMonitoring(videoView);
        });

        videoView.setOnCompletionListener(mp -> {
            Log.d(TAG, "Video completed");
            stopPlaybackMonitoring();
            hideAllActivations();
        });
    }

    private void startPlaybackMonitoring(VideoView videoView) {
        stopPlaybackMonitoring();

        playbackRunnable = new Runnable() {
            @Override
            public void run() {
                if (!videoView.isPlaying() && activeDetailActivation == null) {
                    playbackHandler.postDelayed(this, 500);
                    return;
                }

                if (!isInSeek) {
                    int currentPosition = videoView.getCurrentPosition();
                    if (currentPosition != lastProcessedPosition) {
                        updateActivations(currentPosition);
                        lastProcessedPosition = currentPosition;
                    }
                }

                playbackHandler.postDelayed(this, 15);
            }
        };

        playbackHandler.post(playbackRunnable);
    }

    private void updateActivations(int currentPosition) {
        if (activeDetailActivation != null) return;
        Map<String, Integer> newActivations = new HashMap<>();

        // Determine what should be active at this position
        for (Distribution.ActivationInstance instance : currentDistribution.activations) {
          List<Distribution.TimeWindow> activeWindows = instance.getActiveWindows(currentPosition);
          for (Distribution.TimeWindow window : activeWindows) {
              String position = window.getPosition();
              if (positionContainers.containsKey(position)) {
                  newActivations.put(position, instance.externalId);
              }
          }
        }

        // First deactivate everything that shouldn't be active anymore
        for (Map.Entry<String, ViewGroup> entry : positionContainers.entrySet()) {
            String position = entry.getKey();
            Integer currentId = activePositionActivations.get(position);
            Integer newId = newActivations.get(position);

            if (currentId != null && currentId != -1 && !currentId.equals(newId)) {
                Activation current = activations.get(currentId);
                if (current != null) {
                    Log.d(TAG, String.format("Deactivating %d from position %s at %d ms",
                        currentId, position, currentPosition));
                    current.hide();
                    activePositionActivations.put(position, -1);
                }
            }
        }

        // Then handle new activations
        for (Map.Entry<String, ViewGroup> entry : positionContainers.entrySet()) {
            String position = entry.getKey();
            ViewGroup container = entry.getValue();
            Integer currentId = activePositionActivations.get(position);
            Integer newId = newActivations.get(position);

            if (newId != null && newId != -1 && !newId.equals(currentId)) {
                Activation newActivation = activations.get(newId);
                if (newActivation != null) {
                    View activationView = newActivation.getView();
                    ViewGroup parent = (ViewGroup) activationView.getParent();
                    if (parent != null) {
                        parent.removeView(activationView);
                    }
                    container.removeAllViews();
                    container.addView(activationView);
                    Distribution.TimeWindow window = getTimeWindowForActivation(newId, position, currentPosition);
                    Log.d(TAG, String.format("Activating %d in position %s at %d ms (window: %d-%d ms)",
                        newId, position, currentPosition,
                        window.startTime, window.endTime));
                    newActivation.show();
                    activePositionActivations.put(position, newId);
                }
            }
        }
    }

    private Distribution.TimeWindow getTimeWindowForActivation(int activationId, String position, long currentPosition) {
        for (Distribution.ActivationInstance instance : currentDistribution.activations) {
            if (instance.externalId == activationId) {
                for (int i = 0; i < instance.timeWindows.size(); i++) {
                    Distribution.TimeWindow window = instance.timeWindows.get(i);
                    if (window.getPosition().equals(position) && window.contains(currentPosition)) {
                        Log.d(TAG, String.format("Found window %d for activation %d", i + 1, activationId));
                        return window;
                    }
                }
            }
        }
        return null;
    }

    private void hideAllActivations() {
        for (Activation activation : activations.values()) {
            activation.hide();
        }
        activePositionActivations.clear();
    }

    private void stopPlaybackMonitoring() {
        if (playbackHandler != null && playbackRunnable != null) {
            playbackHandler.removeCallbacks(playbackRunnable);
        }
    }

    public boolean handleBackButton() {
        if (activeDetailActivation != null) {
            Activation activation = activations.get(activeDetailActivation);
            if (activation != null) {
                return activation.handleBackButton();
            }
        }
        return false;
    }

    @Override
    public void onEnterDetailMode(int activationId) {
        activeDetailActivation = activationId;
        for (Map.Entry<Integer, Activation> entry : activations.entrySet()) {
            if (entry.getKey() != activationId) {
                entry.getValue().hide();
            }
        }
    }

    @Override
    public void onExitDetailMode(int activationId) {
        activeDetailActivation = null;
    }

    @Override
    public void onTimeWindowExit(int activationId) {
        if (activeDetailActivation != null && activeDetailActivation == activationId) {
            Activation activation = activations.get(activationId);
            if (activation != null) {
                activation.exitDetailMode();
            }
        }
    }

    public JSONObject getSettings() {
        return settings;
    }
}
