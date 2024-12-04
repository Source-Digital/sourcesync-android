package io.sourcesync.android.activation.components;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.view.Gravity;
import android.graphics.Color;
import android.view.View;

public class ActivationHeader extends FrameLayout {

    public ActivationHeader(Context context, Runnable onClose) {
        super(context);
        initializeView(onClose);
    }

    private void initializeView(Runnable onClose) {
        setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ));

        // Create close button
        ImageButton closeButton = new ImageButton(getContext());
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setOnClickListener(v -> onClose.run());

        LayoutParams buttonParams = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.END | Gravity.CENTER_VERTICAL
        );
        buttonParams.setMargins(16, 16, 16, 16);

        addView(closeButton, buttonParams);
    }
}
