package io.sourcesync.android.segment;

import org.json.JSONObject;
import org.json.JSONException;

public class SegmentSize {
    public final int width;
    public final int height;

    public SegmentSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static SegmentSize fromJson(JSONObject json) throws JSONException {
        return new SegmentSize(
            json.getInt("width"),
            json.getInt("height")
        );
    }
}
