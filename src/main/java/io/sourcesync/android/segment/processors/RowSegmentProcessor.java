package io.sourcesync.android.segment.processors;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.SegmentAttributes;
import io.sourcesync.android.segment.LayoutUtils;
import io.sourcesync.android.segment.factory.SegmentProcessorFactory;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

public class RowSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "RowSegmentProcessor";
    private final JSONObject settings;
    private final SegmentProcessorFactory processorFactory;
    private final ViewGroup parentContainer;

    public RowSegmentProcessor(JSONObject settings, SegmentProcessorFactory processorFactory, ViewGroup parentContainer) {
        this.settings = settings;
        this.processorFactory = processorFactory;
        this.parentContainer = parentContainer;
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        JSONObject attributesJson = segment.optJSONObject("attributes");
        SegmentAttributes attributes = attributesJson != null ?
            SegmentAttributes.fromJson(attributesJson) : null;

        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set row alignment
        if (attributes != null && attributes.alignment != null) {
            rowLayout.setGravity(LayoutUtils.getGravityFromAlignment(attributes.alignment));
        } else {
            rowLayout.setGravity(android.view.Gravity.CENTER);
        }

        // Configure layout parameters for the row
        LinearLayout.LayoutParams rowParams;
        if (attributes != null && attributes.width != null) {
            // If width is specified as percentage, calculate pixels
            int parentWidth = parentContainer.getWidth();
            if (parentWidth == 0) {
                parentWidth = parentContainer.getMeasuredWidth();
            }
            int width = LayoutUtils.percentageToPx(context, attributes.width, parentWidth);
            rowParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
        rowLayout.setLayoutParams(rowParams);

        // Apply spacing between children if specified
        if (attributes != null && attributes.spacing != null) {
            try {
                // Get spacing value from settings using the token
                int spacingDp = settings.getJSONObject("sizeTokens")
                    .getInt(attributes.spacing.toString().toLowerCase());
                int spacingPx = LayoutUtils.dpToPx(context, spacingDp);
                rowLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                rowLayout.setDividerPadding(spacingPx);
            } catch (JSONException e) {
                Log.w(TAG, "Error getting spacing from settings", e);
            }
        }

        // Process children
        JSONArray children = segment.optJSONArray("children");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject childSegment = children.getJSONObject(i);
                String childType = childSegment.getString("type");

                SegmentProcessor processor = processorFactory.getProcessor(childType);
                if (processor != null) {
                    View childView = processor.processSegment(context, childSegment);
                    if (childView != null) {
                        rowLayout.addView(childView);

                        // If child has percentage width, adjust its layout params
                        JSONObject childAttributes = childSegment.optJSONObject("attributes");
                        if (childAttributes != null) {
                            SegmentAttributes childAttrs = SegmentAttributes.fromJson(childAttributes);
                            if (childAttrs.width != null && LayoutUtils.isValidPercentage(childAttrs.width)) {
                                LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(
                                    0, // Width will be determined by weight
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LayoutUtils.percentageToDecimal(childAttrs.width)
                                );
                                childView.setLayoutParams(childParams);
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "No processor found for child segment type: " + childType);
                }
            }
        }

        return rowLayout;
    }

    @Override
    public String getSegmentType() {
        return "row";
    }
}