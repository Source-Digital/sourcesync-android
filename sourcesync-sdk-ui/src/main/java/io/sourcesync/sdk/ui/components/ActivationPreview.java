package io.sourcesync.android.components;

import android.content.Context;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;
import io.sourcesync.android.segment.factory.SegmentProcessorFactory;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.LayoutUtils;

public class ActivationPreview extends LinearLayout {
    private static final String TAG = "ActivationPreview";
    private LinearLayout contentContainer;
    private final SegmentProcessorFactory processorFactory;

    public ActivationPreview(Context context, JSONObject previewData) throws JSONException {
        super(context);
        this.processorFactory = new SegmentProcessorFactory(contentContainer);
        initializeView(previewData);
    }

    private void initializeView(JSONObject previewData) throws JSONException {
        setOrientation(LinearLayout.VERTICAL);

        // Create content container
        contentContainer = new LinearLayout(getContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Apply background color and opacity
        float opacity = (float) previewData.optDouble("backgroundOpacity", 0.66);
        String backgroundColor = previewData.optString("backgroundColor", "#000000");
        int color = Color.parseColor(backgroundColor);
        contentContainer.setBackgroundColor(Color.argb(
            (int)(opacity * 255),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        ));

        // Add padding
        int padding = LayoutUtils.dpToPx(getContext(), 16);
        contentContainer.setPadding(padding, padding, padding, padding);

        addView(contentContainer);
        setClickable(true);
        setFocusable(true);

        // Process template if provided
        if (previewData.has("template")) {
            processTemplate(previewData.getJSONArray("template"));
        } else {
            // Use default template if none provided
            processTemplate(createDefaultTemplate(previewData));
        }
    }

    private void processTemplate(JSONArray template) throws JSONException {
        for (int i = 0; i < template.length(); i++) {
            JSONObject segment = template.getJSONObject(i);
            String segmentType = segment.getString("type");
            
            SegmentProcessor processor = processorFactory.getProcessor(segmentType);
            if (processor != null) {
                View segmentView = processor.processSegment(getContext(), segment);
                if (segmentView != null) {
                    contentContainer.addView(segmentView);
                }
            } else {
                Log.w(TAG, "No processor found for segment type: " + segmentType);
            }
        }
    }

    private JSONArray createDefaultTemplate(JSONObject previewData) throws JSONException {
        JSONArray template = new JSONArray();
        
        // Add title if present
        if (previewData.has("title")) {
            template.put(new JSONObject()
                .put("type", "text")
                .put("content", previewData.getString("title"))
                .put("attributes", new JSONObject()
                    .put("size", "lg")
                    .put("color", "#FFFFFF")
                    .put("weight", "bold")
                    .put("alignment", "left")));
        }
        
        // Add subtitle if present
        if (previewData.has("subtitle")) {
            template.put(new JSONObject()
                .put("type", "text")
                .put("content", previewData.getString("subtitle"))
                .put("attributes", new JSONObject()
                    .put("size", "md")
                    .put("color", "#CCCCCC")
                    .put("style", "italic")
                    .put("alignment", "left")));
        }
        
        return template;
    }
}