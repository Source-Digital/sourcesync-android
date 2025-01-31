package io.sourcesync.android.segment.processors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.util.Log;

import io.sourcesync.android.segment.SegmentProcessor;
import io.sourcesync.android.segment.SegmentAttributes;
import io.sourcesync.android.segment.LayoutUtils;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.InputStream;
import java.net.URL;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "SourceSync.segment.image";
    private static ImageLoader imageLoader;
    private final ViewGroup parentContainer;

    public ImageSegmentProcessor(ViewGroup parentContainer) {
        this.parentContainer = parentContainer;
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
        }
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        JSONObject attributesJson = segment.optJSONObject("attributes");
        SegmentAttributes attributes = attributesJson != null ?
            SegmentAttributes.fromJson(attributesJson) : null;

        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Default dimensions
        int desiredWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
        int desiredHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

        // Set initial layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            desiredWidth,
            desiredHeight
        );

        if (attributes != null && attributes.alignment != null) {
            params.gravity = LayoutUtils.getGravityFromAlignment(attributes.alignment);
        } else {
            params.gravity = android.view.Gravity.CENTER;
        }

        imageView.setLayoutParams(params);

        // Add post-layout listener for size adjustments
        imageView.post(() -> {
            ViewGroup parent = (ViewGroup) imageView.getParent();
            if (parent != null) {
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                Log.d(TAG, String.format("Post-layout - Parent dimensions: %dx%d", parentWidth, parentHeight));

                if (attributesJson != null) {
                    try {
                        JSONObject sizeObj = attributesJson.optJSONObject("size");
                        if (sizeObj != null) {
                            String widthPercent = sizeObj.getString("width");
                            String heightPercent = sizeObj.getString("height");

                            if (!LayoutUtils.isValidPercentage(widthPercent) ||
                                !LayoutUtils.isValidPercentage(heightPercent)) {
                                Log.e(TAG, "Invalid percentage format for image dimensions");
                                return;
                            }

                            int calculatedWidth = Math.round(parentWidth *
                                LayoutUtils.percentageToDecimal(widthPercent));
                            int calculatedHeight = Math.round(parentHeight *
                                LayoutUtils.percentageToDecimal(heightPercent));

                            Log.d(TAG, String.format("Post-layout - Calculated dimensions: %dx%d",
                                calculatedWidth, calculatedHeight));

                            LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                                calculatedWidth,
                                calculatedHeight
                            );
                            newParams.gravity = params.gravity;
                            imageView.setLayoutParams(newParams);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing size attributes", e);
                    }
                }
            }
        });

        String imageUrl = segment.optString("content");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, String.format("Starting image load for URL: %s", imageUrl));
            imageLoader.loadImage(imageUrl, imageView);
        }

        return imageView;
    }

    @Override
    public String getSegmentType() {
        return "image";
    }

    private static class ImageLoader {
        private final HandlerThread handlerThread;
        private final Handler backgroundHandler;
        private final Handler mainHandler;
        private final AtomicBoolean isActive = new AtomicBoolean(true);

        public ImageLoader() {
            handlerThread = new HandlerThread("ImageLoaderThread");
            handlerThread.start();
            backgroundHandler = new Handler(handlerThread.getLooper());
            mainHandler = new Handler(Looper.getMainLooper());
        }

        public void loadImage(String url, ImageView imageView) {
            WeakReference<ImageView> imageViewRef = new WeakReference<>(imageView);
            imageView.setTag(url);

            backgroundHandler.post(() -> {
                if (!isActive.get()) return;

                try {
                    InputStream in = new URL(url).openStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);

                    if (!isActive.get()) return;

                    mainHandler.post(() -> {
                        ImageView view = imageViewRef.get();
                        if (view != null && url.equals(view.getTag())) {
                            view.setImageBitmap(bitmap);
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image: " + url, e);
                    if (!isActive.get()) return;

                    mainHandler.post(() -> {
                        ImageView view = imageViewRef.get();
                        if (view != null && url.equals(view.getTag())) {
                            view.setBackgroundColor(Color.GRAY);
                        }
                    });
                }
            });
        }

        public void shutdown() {
            isActive.set(false);
            handlerThread.quitSafely();
        }
    }
}
