package io.sourcesync.android.segment;

import android.content.Context;
import android.view.View;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Interface for processing different types of segments in the SourceSync system.
 * Each segment type (text, image, button, row, column) should have its own implementation.
 */
public interface SegmentProcessor {
    /**
     * Process a segment JSON object and return an appropriate Android View
     *
     * @param context Android context for creating views
     * @param segment JSON object containing segment data and attributes
     * @return A configured Android View representing the segment
     * @throws JSONException if the segment data is invalid or required fields are missing
     */
    View processSegment(Context context, JSONObject segment) throws JSONException;

    /**
     * Get the type of segment this processor handles
     *
     * @return String identifier for the segment type (e.g., "text", "image", "button", "row", "column")
     */
    String getSegmentType();
}
