package io.sourcesync.android.activation.transitions;

import android.view.View;
import android.view.animation.Animation;

public interface ActivationTransition {
    void enterDetail(View previewView, View detailView, Runnable onComplete);
    void exitDetail(View detailView, View previewView, Runnable onComplete);
}
