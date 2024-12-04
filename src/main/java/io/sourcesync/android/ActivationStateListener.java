package io.sourcesync.android.activation;

public interface ActivationStateListener {
    void onEnterDetailMode(int activationId);
    void onExitDetailMode(int activationId);
    void onTimeWindowExit(int activationId);
}
