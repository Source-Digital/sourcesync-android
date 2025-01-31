package io.sourcesync.android.segment;

import android.content.Context;
import android.view.Gravity;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LayoutUtils {
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)%$");

    public static int getGravityFromAlignment(String alignment) {
        if (alignment == null) return Gravity.CENTER;

        switch (alignment.toLowerCase()) {
            case "left":
                return Gravity.START;
            case "right":
                return Gravity.END;
            case "center":
            default:
                return Gravity.CENTER;
        }
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Validates if a string is a valid percentage value (e.g. "50%")
     * @param value The string to validate
     * @return true if the string is a valid percentage, false otherwise
     */
    public static boolean isValidPercentage(String value) {
        if (value == null) return false;
        Matcher matcher = PERCENTAGE_PATTERN.matcher(value);
        if (!matcher.matches()) return false;

        float percentage = Float.parseFloat(matcher.group(1));
        return percentage >= 0 && percentage <= 100;
    }

    /**
     * Converts a percentage string to a decimal value (e.g. "50%" → 0.5f)
     * @param percentage The percentage string to convert
     * @return The decimal value
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static float percentageToDecimal(String percentage) {
        if (!isValidPercentage(percentage)) {
            throw new IllegalArgumentException("Invalid percentage value: " + percentage);
        }

        Matcher matcher = PERCENTAGE_PATTERN.matcher(percentage);
        matcher.matches(); // We know this succeeds because of isValidPercentage
        return Float.parseFloat(matcher.group(1)) / 100f;
    }

    /**
     * Converts a percentage string to pixels based on a total dimension
     * @param context The Android context
     * @param percentage The percentage string (e.g. "50%")
     * @param totalDimension The total dimension in pixels
     * @return The calculated pixels
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static int percentageToPx(Context context, String percentage, int totalDimension) {
        float decimal = percentageToDecimal(percentage);
        return Math.round(totalDimension * decimal);
    }
}
