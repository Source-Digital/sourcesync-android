package io.sourcesync.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class Distribution {
    private static final String TAG = "SourceSync.distribution";

    public final int id;
    public final String name;
    public final List<ActivationInstance> activations;

    private Distribution(int id, String name, List<ActivationInstance> activations) {
        this.id = id;
        this.name = name;
        this.activations = activations;
    }

    public static Distribution fromJson(JSONObject apiResponse) throws JSONException {
        // Get the first distribution from the data array
        JSONArray dataArray = apiResponse.getJSONArray("data");
        if (dataArray.length() == 0) {
            throw new JSONException("No distribution data found");
        }

        JSONObject distributionData = dataArray.getJSONObject(0);

        return new Distribution(
            distributionData.getInt("id"),
            distributionData.getString("name"),
            parseActivations(distributionData.getJSONObject("data")
                .getJSONObject("timeline")
                .getJSONObject("activations")
                .getJSONArray("items"))
        );
    }

    private static List<ActivationInstance> parseActivations(JSONArray items) throws JSONException {
        Log.d(TAG, "Parsing " + items.length() + " activation items from distribution");
        List<ActivationInstance> instances = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            ActivationInstance instance = ActivationInstance.fromJson(item);
            Log.d(TAG, String.format("Parsed activation %d with %d time windows:",
                instance.externalId, instance.timeWindows.size()));
            for (TimeWindow window : instance.timeWindows) {
                Log.d(TAG, String.format("  Window: start=%d, end=%d, position=%s",
                    window.startTime, window.endTime, window.position));
            }
            instances.add(instance);
        }
        return instances;
    }

    public static class ActivationInstance {
        public final int externalId;
        public final List<TimeWindow> timeWindows;

        private ActivationInstance(int externalId, List<TimeWindow> timeWindows) {
            this.externalId = externalId;
            this.timeWindows = timeWindows;
        }

        public static ActivationInstance fromJson(JSONObject json) throws JSONException {
            int externalId = json.getInt("externalId");
            List<TimeWindow> timeWindows = new ArrayList<>();

            JSONArray instances = json.getJSONArray("instances");
            for (int i = 0; i < instances.length(); i++) {
                JSONObject instance = instances.getJSONObject(i);
                timeWindows.add(TimeWindow.fromJson(instance));
            }

            return new ActivationInstance(externalId, timeWindows);
        }

        public List<TimeWindow> getActiveWindows(long videoPosition, boolean shouldLog) {
            List<TimeWindow> activeWindows = new ArrayList<>();
            for (TimeWindow window : timeWindows) {
                if (window.contains(videoPosition)) {
                    activeWindows.add(window);
                }
            }
            return activeWindows;
        }

        public List<TimeWindow> getActiveWindows(long videoPosition) {
            return getActiveWindows(videoPosition, false);
        }

        public boolean isActiveAt(long videoPosition) {
            return !getActiveWindows(videoPosition, false).isEmpty();
        }

        public TimeWindow getCurrentWindow(long videoPosition) {
            List<TimeWindow> active = getActiveWindows(videoPosition, false);
            return active.isEmpty() ? null : active.get(0);
        }
    }

    public static class TimeWindow {
        public final long startTime;
        public final long endTime;
        public final JSONObject settings;
        public final String position;

        private TimeWindow(long startTime, long endTime, JSONObject settings) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.settings = settings;
            this.position = "top"; // Default position since new API doesn't specify it
        }

        public static TimeWindow fromJson(JSONObject json) throws JSONException {
            JSONObject when = json.getJSONObject("when");
            return new TimeWindow(
                when.getLong("start"),
                when.getLong("end"),
                json.optJSONObject("settings")
            );
        }

        public boolean contains(long videoPosition) {
            return videoPosition >= startTime && videoPosition < endTime;
        }

        public String getPosition() {
            return position;
        }
    }
}
