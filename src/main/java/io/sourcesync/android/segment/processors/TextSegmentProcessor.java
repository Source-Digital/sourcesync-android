package io.sourcesync.android.segment.processors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.SegmentAttributes;
import io.sourcesync.android.segment.LayoutUtils;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class TextSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "TextSegmentProcessor";

    public TextSegmentProcessor() {}

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        String content = segment.getString("content");
        JSONObject attributesJson = segment.optJSONObject("attributes");
        SegmentAttributes attributes = attributesJson != null ?
            SegmentAttributes.fromJson(attributesJson) : null;

        TextView textView = new TextView(context);
        SpannableStringBuilder builder = new SpannableStringBuilder(content);

        if (attributes != null) {
            applyTextAttributes(context, builder, attributes, 0, content.length());

            if (attributes.alignment != null) {
                textView.setGravity(LayoutUtils.getGravityFromAlignment(attributes.alignment));
            }

            // Handle width if specified
            LinearLayout.LayoutParams params;
            if (attributes.width != null && LayoutUtils.isValidPercentage(attributes.width)) {
                params = new LinearLayout.LayoutParams(
                    0, // Width will be determined by weight
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LayoutUtils.percentageToDecimal(attributes.width)
                );
            } else {
                params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }
            textView.setLayoutParams(params);
        }

        textView.setText(builder);
        return textView;
    }

    @Override
    public String getSegmentType() {
        return "text";
    }

    private void applyTextAttributes(Context context,
                                   SpannableStringBuilder builder,
                                   SegmentAttributes attributes,
                                   int start,
                                   int end) {
        if (attributes == null) return;

        // Apply font size using hardcoded size tokens
        if (attributes.fontSize != null) {
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
            builder.setSpan(
                new AbsoluteSizeSpan(dpSize, true),
                start, end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply text color
        if (attributes.color != null) {
            try {
                int color = Color.parseColor(attributes.color);
                builder.setSpan(
                    new ForegroundColorSpan(color),
                    start, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid color format: " + attributes.color, e);
            }
        }

        // Apply text style (bold)
        if ("bold".equals(attributes.weight)) {
            builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                start, end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply text style (italic)
        if ("italic".equals(attributes.style)) {
            builder.setSpan(
                new StyleSpan(Typeface.ITALIC),
                start, end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply underline
        if (Boolean.TRUE.equals(attributes.underline)) {
            builder.setSpan(
                new UnderlineSpan(),
                start, end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
}