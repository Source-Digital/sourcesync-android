package io.sourcesync.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.json.JSONObject;
import org.json.JSONException;
import io.sourcesync.android.components.ActivationPreview;
import io.sourcesync.android.components.ActivationDetail;

public class Activation extends FrameLayout {
    private ActivationPreview previewView;
    private ActivationDetail detailView;

    public Activation(Context context) {
        super(context);
    }

    public void showPreview(JSONObject previewData, OnClickListener onClickListener) {
        try {
            if (previewView != null) {
                removeView(previewView);
            }
            previewView = new ActivationPreview(getContext(), previewData);
            previewView.setOnClickListener(onClickListener);
            addView(previewView);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid preview data", e);
        }
    }

    public void showDetail(JSONObject detailData, Runnable onClose) {
        try {
            if (detailView != null) {
                removeView(detailView);
            }
            if (previewView != null) {
                previewView.setVisibility(View.GONE);
            }
            detailView = new ActivationDetail(getContext(), 
                detailData.getJSONArray("template"), 
                onClose);
            addView(detailView);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid detail data", e);
        }
    }

    public void hideDetail() {
        if (detailView != null) {
            removeView(detailView);
            detailView = null;
        }
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }
}