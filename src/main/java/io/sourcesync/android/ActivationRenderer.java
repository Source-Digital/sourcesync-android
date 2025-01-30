// ActivationRenderer.java
package io.sourcesync.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.json.JSONObject;
import org.json.JSONException;

public class ActivationRenderer {
    private final Context context;
    private ActivationPreview previewView;
    private ActivationDetail detailView;
    private ViewGroup container;

    public ActivationRenderer(Context context, ViewGroup container) {
        this.context = context;
        this.container = container;
    }

    public void showPreview(JSONObject previewData, View.OnClickListener onClickListener) {
        try {
            // Clean up any existing views
            if (previewView != null && previewView.getParent() != null) {
                ((ViewGroup) previewView.getParent()).removeView(previewView);
            }
            if (detailView != null && detailView.getParent() != null) {
                ((ViewGroup) detailView.getParent()).removeView(detailView);
            }

            // Create new preview
            previewView = new ActivationPreview(context, previewData);
            previewView.setOnClickListener(onClickListener);
            container.addView(previewView);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid preview data", e);
        }
    }

    public void showDetail(JSONObject detailData, Runnable onClose) {
        try {
            // Hide preview if it exists
            if (previewView != null) {
                previewView.setVisibility(View.GONE);
            }

            // Create and show detail view
            detailView = new ActivationDetail(context, detailData.getJSONArray("template"), onClose);
            container.addView(detailView);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid detail data", e);
        }
    }

    public void hideDetail() {
        if (detailView != null && detailView.getParent() != null) {
            ((ViewGroup) detailView.getParent()).removeView(detailView);
            detailView = null;
        }
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }
}