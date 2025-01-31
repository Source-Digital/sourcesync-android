package io.sourcesync.sdk.ui.demo_mobile;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import io.sourcesync.android.Activation;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    private Activation activation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create container for activation views
        FrameLayout container = new FrameLayout(this);
        setContentView(container);
        
        // Create activation
        activation = new Activation(this);
        container.addView(activation);

        try {
            // Show preview
            JSONObject previewData = new JSONObject()
                .put("title", "Demo Preview")
                .put("subtitle", "Click to see details")
                .put("backgroundColor", "#000000")
                .put("backgroundOpacity", 0.66);
                
            activation.showPreview(previewData, v -> showDetail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetail() {
        try {
            JSONObject detailData = new JSONObject()
                .put("template", new JSONArray()
                    .put(new JSONObject()
                        .put("type", "text")
                        .put("content", "Welcome to SourceSync!")
                        .put("attributes", new JSONObject()
                            .put("size", "lg")
                            .put("color", "#FFFFFF")))
                    .put(new JSONObject()
                        .put("type", "button")
                        .put("content", "Close")
                        .put("attributes", new JSONObject()
                            .put("backgroundColor", "#4CAF50")
                            .put("textColor", "#FFFFFF"))));
                        
            activation.showDetail(detailData, () -> activation.hideDetail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}