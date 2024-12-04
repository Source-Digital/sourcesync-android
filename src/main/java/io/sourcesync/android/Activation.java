package io.sourcesync.android.activation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.sourcesync.android.activation.components.ActivationPreview;
import io.sourcesync.android.activation.components.ActivationDetail;
import io.sourcesync.android.activation.transitions.ActivationTransition;
import io.sourcesync.android.activation.transitions.FadeTransition;

public class Activation {
    private static final String TAG = "SourceSync.activation";

    public final int id;
    public final String name;
    public final JSONObject settings;
    public final JSONArray template;

    private ActivationMode currentMode = ActivationMode.PREVIEW;
    private ActivationStateListener stateListener;
    private FrameLayout container;
    private ActivationPreview previewView;
    private ActivationDetail detailView;
    private Context context;
    private ActivationTransition transition;
    private boolean isVisible = false;
    private ViewGroup lastParent = null;
    private JSONObject sdkSettings;

    private Activation(int id, String name, JSONObject settings, JSONArray template) {
        this.id = id;
        this.name = name;
        this.settings = settings;
        this.template = template;
        Log.d(TAG, "Created activation " + id);
    }

    public static Activation fromJson(JSONObject json) throws JSONException {
        return new Activation(
            json.getInt("id"),
            json.getString("name"),
            json.getJSONObject("settings"),
            json.getJSONArray("template")
        );
    }

    public void initialize(Context context, ActivationStateListener listener, JSONObject settings) {
        Log.d(TAG, "Initializing activation " + id);
        this.context = context;
        this.stateListener = listener;
        this.sdkSettings = settings;
        this.transition = new FadeTransition();

        container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(params);
        Log.d(TAG, "Created container for activation " + id);

        createPreviewView();
        createDetailView();
    }

    private void createPreviewView() {
        Log.d(TAG, "Creating preview view for activation " + id);
        previewView = new ActivationPreview(context, settings, sdkSettings);
        previewView.setOnClickListener(v -> {
            Log.d(TAG, String.format("%d: detail requested", id));
            enterDetailMode();
        });
        Log.d(TAG, "Successfully created preview view for activation " + id);
    }

    private void createDetailView() {
        Log.d(TAG, "Creating detail view for activation " + id);
        detailView = new ActivationDetail(context, template, () -> exitDetailMode(), sdkSettings);
        detailView.setVisibility(View.GONE);
        Log.d(TAG, "Created detail view for activation " + id);
    }

    public View getView() {
        return container;
    }

    public void show() {
        if (!isVisible) {
            Log.d(TAG, String.format("%d: Showing %s", id, currentMode));
            if (currentMode == ActivationMode.PREVIEW) {
                lastParent = (ViewGroup) container.getParent();
                container.removeAllViews();
                container.addView(previewView);
                container.setVisibility(View.VISIBLE);
                isVisible = true;
                Log.d(TAG, String.format("%d: Preview view added and made visible", id));
            }
        }
    }

    public void hide() {
        if (isVisible) {
            Log.d(TAG, String.format("%d: Hiding %s", id, currentMode));
            if (currentMode == ActivationMode.DETAIL) {
                exitDetailMode();
            } else {
                container.setVisibility(View.GONE);
            }
            isVisible = false;
        }
    }

    public void enterDetailMode() {
        if (currentMode == ActivationMode.PREVIEW && isVisible) {
            Log.d(TAG, String.format("%d: Entering detail mode", id));
            currentMode = ActivationMode.DETAIL;

            View decorView = container.getRootView();
            ViewGroup rootLayout = (ViewGroup) decorView;

            lastParent = (ViewGroup) container.getParent();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            );
            detailView.setLayoutParams(params);

            if (detailView.getParent() != null) {
                ((ViewGroup) detailView.getParent()).removeView(detailView);
            }

            container.setVisibility(View.VISIBLE);
            rootLayout.addView(detailView);

            if (transition != null) {
                transition.enterDetail(previewView, detailView, () -> {
                    if (container.getParent() != null) {
                        ((ViewGroup) container.getParent()).removeView(container);
                    }
                    if (stateListener != null) {
                        Log.d(TAG, String.format("%d: detail mode enter completed", id));
                        stateListener.onEnterDetailMode(id);
                    }
                });
            } else {
                if (container.getParent() != null) {
                    ((ViewGroup) container.getParent()).removeView(container);
                }
                detailView.setVisibility(View.VISIBLE);
                if (stateListener != null) {
                    stateListener.onEnterDetailMode(id);
                }
            }
        }
    }

    public void exitDetailMode() {
        if (currentMode == ActivationMode.DETAIL) {
            Log.d(TAG, String.format("%d: Exiting detail mode", id));
            currentMode = ActivationMode.PREVIEW;

            if (transition != null) {
                transition.exitDetail(detailView, previewView, () -> {
                    Log.d(TAG, String.format("%d: Exit transition complete", id));
                    if (lastParent != null && container.getParent() == null) {
                        lastParent.addView(container);
                    }
                    container.removeAllViews();
                    container.addView(previewView);
                    container.setVisibility(View.VISIBLE);

                    cleanupDetailView();
                    if (stateListener != null) {
                        Log.d(TAG, String.format("%d: Detail mode exit completed", id));
                        stateListener.onExitDetailMode(id);
                    }
                });
            } else {
                if (lastParent != null && container.getParent() == null) {
                    lastParent.addView(container);
                }
                container.removeAllViews();
                container.addView(previewView);
                container.setVisibility(View.VISIBLE);
                cleanupDetailView();

                if (stateListener != null) {
                    stateListener.onExitDetailMode(id);
                }
            }
        }
    }

    private void cleanupDetailView() {
        if (detailView.getParent() != null) {
            ((ViewGroup) detailView.getParent()).removeView(detailView);
        }
        detailView.setVisibility(View.GONE);
    }

    public boolean handleBackButton() {
        if (currentMode == ActivationMode.DETAIL) {
            Log.d(TAG, String.format("%d: Back pressed in detail mode", id));
            exitDetailMode();
            return true;
        }
        return false;
    }

    public boolean isInDetailMode() {
        return currentMode == ActivationMode.DETAIL;
    }

    public void onTimeWindowExit() {
        if (stateListener != null) {
            stateListener.onTimeWindowExit(id);
        }
    }
}
