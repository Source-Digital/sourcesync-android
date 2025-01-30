package io.sourcesync.android.components;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.view.MotionEvent;
import android.util.Log;
import io.sourcesync.android.segment.factory.SegmentProcessorFactory;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.LayoutUtils;

public class ActivationDetail extends FrameLayout {
    private static final String TAG = "ActivationDetail";
    private SegmentProcessorFactory processorFactory;
    private final LinearLayout contentContainer;
    private final ScrollView scrollView;

    public ActivationDetail(Context context, JSONArray template, Runnable onClose) {
        super(context);
        
        // Semi-transparent background
        setBackgroundColor(Color.argb(200, 0, 0, 0));

        // Create header
        ActivationHeader header = new ActivationHeader(context, onClose);

        // Create ScrollView for scrollable content
        scrollView = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0  // Use weight for height
        );
        scrollParams.weight = 1;
        scrollView.setLayoutParams(scrollParams);
        scrollView.setFillViewport(true);

        // Create content container
        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        contentContainer.setGravity(Gravity.CENTER_HORIZONTAL);

        // Add padding
        int padding = LayoutUtils.dpToPx(getContext(), 16);
        contentContainer.setPadding(padding, padding, padding, padding);

        // Initialize processor factory
        processorFactory = new SegmentProcessorFactory(contentContainer);

        // Assemble the view hierarchy
        scrollView.addView(contentContainer);

        // Create a container for header and scrollview
        LinearLayout mainContainer = new LinearLayout(context);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Add header and scrollview to main container
        mainContainer.addView(header);
        mainContainer.addView(scrollView);

        // Add main container to this FrameLayout
        addView(mainContainer);

        // Process template
        processTemplate(template);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Consume all touch events to prevent them from propagating
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Don't intercept touch events to allow scrolling and clicking of child views
        return false;
    }

    private void processTemplate(JSONArray template) {
        try {
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
        } catch (JSONException e) {
            Log.e(TAG, "Error processing detail template", e);
        }
    }
}