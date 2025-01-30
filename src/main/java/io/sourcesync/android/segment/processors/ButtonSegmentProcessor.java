package io.sourcesync.android.segment.processors;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.SegmentAttributes;
import io.sourcesync.android.segment.LayoutUtils;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class ButtonSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "ButtonSegmentProcessor";

    public ButtonSegmentProcessor() {}

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        String content = segment.getString("content");
        JSONObject attributesJson = segment.optJSONObject("attributes");
        SegmentAttributes attributes = attributesJson != null ?
            SegmentAttributes.fromJson(attributesJson) : null;

        Button button = new Button(context);
        button.setText(content);

        if (attributes != null) {
            // Apply background color
            if (attributes.backgroundColor != null) {
                try {
                    button.setBackgroundColor(Color.parseColor(attributes.backgroundColor));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid background color format: " + attributes.backgroundColor, e);
                }
            }

            // Apply text color
            if (attributes.textColor != null) {
                try {
                    button.setTextColor(Color.parseColor(attributes.textColor));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid text color format: " + attributes.textColor, e);
                }
            }

            // Apply font size - using dp values directly
            if (attributes.fontSize != null) {
                // Map size tokens to dp values
                int dpSize;
                switch (attributes.fontSize.toLowerCase()) {
                    case "xxs": dpSize = 6; break;
                    case "xs": dpSize = 10; break;
                    case "sm": dpSize = 14; break;
                    case "md": dpSize = 16; break;
                    case "lg": dpSize = 20; break;
                    case "xl": dpSize = 24; break;
                    case "xxl": dpSize = 32; break;
                    default: dpSize = 16; break;
                }
                button.setTextSize(dpSize);
            }

            // Handle width if specified as percentage
            LinearLayout.LayoutParams params;
            if (attributes.width != null && LayoutUtils.isValidPercentage(attributes.width)) {
                float weight = LayoutUtils.percentageToDecimal(attributes.width);
                params = new LinearLayout.LayoutParams(
                    0, // Width will be determined by weight
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight
                );
            } else {
                params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }

            // Apply alignment
            if (attributes.alignment != null) {
                params.gravity = LayoutUtils.getGravityFromAlignment(attributes.alignment);
            } else {
                params.gravity = android.view.Gravity.CENTER;
            }

            button.setLayoutParams(params);
        }

        return button;
    }

    @Override
    public String getSegmentType() {
        return "button";
    }
}