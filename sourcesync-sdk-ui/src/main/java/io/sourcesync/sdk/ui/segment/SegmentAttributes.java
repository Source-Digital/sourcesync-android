package io.sourcesync.android.segment;

import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class SegmentAttributes {
    private static final String TAG = "SourceSync.SegmentAttrs";

    public String font;
    public String fontSize;  // Changed from SizeToken to String
    public String color;
    public String weight;
    public String style;
    public Boolean underline;
    public String backgroundColor;
    public String textColor;
    public String spacing;   // Changed from Integer to String for token support
    public String width;     // Stores percentage (e.g. "50%")
    public String height;    // Stores percentage (e.g. "100%")
    public String alignment;
    public String contentMode;

    public static SegmentAttributes fromJson(JSONObject json) throws JSONException {
        SegmentAttributes attrs = new SegmentAttributes();

        // Basic attributes
        if (json.has("font")) attrs.font = json.getString("font");
        if (json.has("color")) attrs.color = json.getString("color");
        if (json.has("weight")) attrs.weight = json.getString("weight");
        if (json.has("style")) attrs.style = json.getString("style");
        if (json.has("underline")) attrs.underline = json.getBoolean("underline");
        if (json.has("backgroundColor")) attrs.backgroundColor = json.getString("backgroundColor");
        if (json.has("textColor")) attrs.textColor = json.getString("textColor");
        if (json.has("alignment")) attrs.alignment = json.getString("alignment");
        if (json.has("contentMode")) attrs.contentMode = json.getString("contentMode");

        // Handle size tokens
        if (json.has("size")) {
            Object size = json.get("size");
            if (size instanceof JSONObject) {
                // Handle dimension object
                JSONObject dimObj = (JSONObject) size;
                String width = dimObj.getString("width");
                String height = dimObj.getString("height");

                if (!LayoutUtils.isValidPercentage(width) || !LayoutUtils.isValidPercentage(height)) {
                    throw new JSONException("Dimensions must be percentage values");
                }

                attrs.width = width;
                attrs.height = height;
            } else {
                // Handle font size token
                attrs.fontSize = size.toString().toLowerCase();
            }
        }

        // Handle spacing token
        if (json.has("spacing")) {
            attrs.spacing = json.get("spacing").toString().toLowerCase();
        }

        // Handle direct width/height
        if (json.has("width")) {
            String width = json.getString("width");
            if (!LayoutUtils.isValidPercentage(width)) {
                throw new JSONException("Width must be a percentage value");
            }
            attrs.width = width;
        }

        if (json.has("height")) {
            String height = json.getString("height");
            if (!LayoutUtils.isValidPercentage(height)) {
                throw new JSONException("Height must be a percentage value");
            }
            attrs.height = height;
        }

        // Handle fontSize when specified directly
        if (json.has("fontSize")) {
            attrs.fontSize = json.getString("fontSize").toLowerCase();
        }

        return attrs;
    }
}
