package io.sourcesync.android.activation.components;

import android.content.Context;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;
import android.widget.FrameLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.sourcesync.android.segment.processors.*;
import io.sourcesync.android.segment.factory.SegmentProcessorFactory;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.LayoutUtils;

public class ActivationPreview extends LinearLayout {
    private static final String TAG = "ActivationPreview";
    private final SegmentProcessorFactory processorFactory;
    private final JSONObject settings;
    private LinearLayout contentContainer;

    public ActivationPreview(Context context, JSONObject activationSettings, JSONObject sdkSettings) {
        super(context);
        this.settings = sdkSettings;
        this.processorFactory = SegmentProcessorFactory.getInstance(settings, contentContainer);
        initializeView();

        try {
            JSONObject previewSettings = activationSettings.getJSONObject("preview");
            if (previewSettings.has("template")) {
                processTemplate(previewSettings.getJSONArray("template"));
            } else {
                processSimplePreview(previewSettings, sdkSettings);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing preview", e);
        }
    }

    private void initializeView() {
        setOrientation(LinearLayout.VERTICAL);

        // Calculate one-third of screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int oneThirdWidth = screenWidth / 3;

        // Set layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            oneThirdWidth,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END | Gravity.TOP;
        setLayoutParams(params);

        // Create content container
        contentContainer = new LinearLayout(getContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Apply semi-transparent black background
        contentContainer.setBackgroundColor(Color.argb(204, 0, 0, 0));

        // Add padding using value from settings
        try {
            int paddingDp = settings.getJSONObject("overlay").getInt("defaultPadding");
            int padding = LayoutUtils.dpToPx(getContext(), paddingDp);
            contentContainer.setPadding(padding, padding, padding, padding);
        } catch (JSONException e) {
            Log.e(TAG, "Error getting padding from settings", e);
            int padding = LayoutUtils.dpToPx(getContext(), 16);
            contentContainer.setPadding(padding, padding, padding, padding);
        }

        addView(contentContainer);
        setClickable(true);
        setFocusable(true);
    }

    private void processSimplePreview(JSONObject previewSettings, JSONObject sdkSettings) throws JSONException {
      // Get default preview settings and template
      JSONObject defaultPreview = sdkSettings.getJSONObject("previews").getJSONObject("default");
      JSONObject defaults = defaultPreview.getJSONObject("defaults");
      JSONArray templateSegments = defaultPreview.getJSONArray("template");

      // Get values from preview settings or use defaults
      String title = previewSettings.optString("title", defaults.getString("title"));
      String subtitle = previewSettings.optString("subtitle", defaults.getString("subtitle"));

      // Set background color and opacity
      int alpha = (int)(defaults.getDouble("backgroundOpacity") * 255);
      int color = Color.parseColor(defaults.getString("backgroundColor"));
      contentContainer.setBackgroundColor(Color.argb(alpha,
          Color.red(color),
          Color.green(color),
          Color.blue(color)));

      // Process template segments
      for (int i = 0; i < templateSegments.length(); i++) {
          JSONObject segment = new JSONObject(templateSegments.getJSONObject(i).toString());

          // Replace template variables
          String content = segment.getString("content")
              .replace("{title}", title)
              .replace("{subtitle}", subtitle);
          segment.put("content", content);

          String segmentType = segment.getString("type");
          SegmentProcessor processor = processorFactory.getProcessor(segmentType);

          if (processor != null) {
              View segmentView = processor.processSegment(getContext(), segment);
              if (segmentView != null) {
                  contentContainer.addView(segmentView);
              }
          }
      }
  }

  private void processTemplate(JSONArray template) {
        try {
            for (int i = 0; i < template.length(); i++) {
                JSONObject block = template.getJSONObject(i);
                if ("NativeBlock".equals(block.getString("name"))) {
                    JSONObject blockSettings = block.getJSONObject("settings");
                    processSegments(blockSettings.getJSONArray("segments"));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing preview template", e);
        }
    }

    private void processSegments(JSONArray segments) throws JSONException {
        for (int i = 0; i < segments.length(); i++) {
            JSONObject segmentJson = segments.getJSONObject(i);
            String segmentType = segmentJson.getString("type");

            SegmentProcessor processor = processorFactory.getProcessor(segmentType);
            if (processor != null) {
                View segmentView = processor.processSegment(getContext(), segmentJson);
                if (segmentView != null) {
                    contentContainer.addView(segmentView);
                }
            } else {
                Log.w(TAG, "No processor found for segment type: " + segmentType);
            }
        }
    }
}
