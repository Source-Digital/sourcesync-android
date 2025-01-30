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

public class ColumnSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "ColumnSegmentProcessor";
    private final SegmentProcessorFactory processorFactory;
    private final ViewGroup parentContainer;

    public ColumnSegmentProcessor(SegmentProcessorFactory processorFactory, ViewGroup parentContainer) {
        this.processorFactory = processorFactory;
        this.parentContainer = parentContainer;
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        JSONObject attributesJson = segment.optJSONObject("attributes");
        SegmentAttributes attributes = attributesJson != null ?
            SegmentAttributes.fromJson(attributesJson) : null;

        LinearLayout columnLayout = new LinearLayout(context);
        columnLayout.setOrientation(LinearLayout.VERTICAL);

        // Set column alignment
        if (attributes != null && attributes.alignment != null) {
            columnLayout.setGravity(LayoutUtils.getGravityFromAlignment(attributes.alignment));
        } else {
            columnLayout.setGravity(android.view.Gravity.CENTER);
        }

        // Calculate dimensions and weights
        LinearLayout.LayoutParams columnParams;
        if (attributes != null && attributes.width != null) {
            if (LayoutUtils.isValidPercentage(attributes.width)) {
                // Calculate weight based on percentage
                float weight = LayoutUtils.percentageToDecimal(attributes.width);
                columnParams = new LinearLayout.LayoutParams(
                    0, // Width will be determined by weight
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight
                );
            } else {
                Log.w(TAG, "Invalid width percentage: " + attributes.width + ". Using WRAP_CONTENT.");
                columnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }
        } else {
            // Default to equal weight distribution
            columnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            );
        }

        // Handle height if specified
        if (attributes != null && attributes.height != null) {
            if (LayoutUtils.isValidPercentage(attributes.height)) {
                int parentHeight = parentContainer.getHeight();
                if (parentHeight == 0) {
                    parentHeight = parentContainer.getMeasuredHeight();
                }
                columnParams.height = LayoutUtils.percentageToPx(context, attributes.height, parentHeight);
            }
        }

        columnLayout.setLayoutParams(columnParams);

        // Apply spacing between children
        if (attributes != null && attributes.spacing != null) {
            int spacingPx = LayoutUtils.dpToPx(context, 8); // Default 8dp spacing
            columnLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            columnLayout.setDividerPadding(spacingPx);
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
                        // Handle child's percentage dimensions if specified
                        JSONObject childAttributes = childSegment.optJSONObject("attributes");
                        if (childAttributes != null) {
                            SegmentAttributes childAttrs = SegmentAttributes.fromJson(childAttributes);
                            if (childAttrs.width != null && LayoutUtils.isValidPercentage(childAttrs.width)) {
                                LinearLayout.LayoutParams childParams = (LinearLayout.LayoutParams) childView.getLayoutParams();
                                float weight = LayoutUtils.percentageToDecimal(childAttrs.width);
                                childParams.width = 0; // Use weight instead of fixed width
                                childParams.weight = weight;
                                childView.setLayoutParams(childParams);
                            }
                        }
                        columnLayout.addView(childView);
                    }
                } else {
                    Log.w(TAG, "No processor found for child segment type: " + childType);
                }
            }
        }

        return columnLayout;
    }

    @Override
    public String getSegmentType() {
        return "column";
    }
}