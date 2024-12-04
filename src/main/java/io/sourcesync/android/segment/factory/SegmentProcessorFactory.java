package io.sourcesync.android.segment.factory;

import android.view.ViewGroup;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.processors.*;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import org.json.JSONObject;

public class SegmentProcessorFactory {
    private static final String TAG = "SegmentProcessorFactory";
    private static SegmentProcessorFactory instance;
    private final Map<String, SegmentProcessor> processors;
    private final JSONObject settings;
    private final ViewGroup parentContainer;

    private SegmentProcessorFactory(JSONObject settings, ViewGroup parentContainer) {
        this.settings = settings;
        this.parentContainer = parentContainer;
        this.processors = new HashMap<>();
        registerDefaultProcessors();
    }

    public static synchronized SegmentProcessorFactory getInstance(JSONObject settings, ViewGroup parentContainer) {
        if (instance == null) {
            instance = new SegmentProcessorFactory(settings, parentContainer);
        }
        return instance;
    }

    private void registerDefaultProcessors() {
        registerProcessor(new TextSegmentProcessor(settings));
        registerProcessor(new ImageSegmentProcessor(parentContainer));
        registerProcessor(new ButtonSegmentProcessor(settings));
        registerProcessor(new RowSegmentProcessor(settings, this, parentContainer));
        registerProcessor(new ColumnSegmentProcessor(settings, this, parentContainer));
    }

    public void registerProcessor(SegmentProcessor processor) {
        processors.put(processor.getSegmentType(), processor);
    }

    public SegmentProcessor getProcessor(String segmentType) {
        SegmentProcessor processor = processors.get(segmentType);
        if (processor == null) {
            Log.w(TAG, "No processor found for segment type: " + segmentType);
        }
        return processor;
    }

    public boolean hasProcessor(String segmentType) {
        return processors.containsKey(segmentType);
    }
}
