# SourceSync Android Renderer

A lightweight Android library for rendering structured content from JSON using a flexible segment-based system.

## Overview

This library provides two main components:
- Preview rendering: A compact view for displaying summarized content
- Detail rendering: A full view for displaying detailed content

Both components use a segment-based rendering system that supports:
- Text with rich formatting
- Images
- Buttons
- Flexible layouts (rows and columns)

## Installation

Add the dependency to your app's build.gradle:

```gradle
dependencies {
    implementation 'com.github.Source-Digital.native-sdk:sourcesync-android:1.0.0'
}
```

## Usage

### Basic Example

```java
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

        // Show preview
        JSONObject previewData = new JSONObject()
            .put("title", "Hello World")
            .put("subtitle", "Click for details")
            .put("backgroundColor", "#000000")
            .put("backgroundOpacity", 0.66);
            
        activation.showPreview(previewData, v -> showDetail());
    }

    private void showDetail() {
        JSONObject detailData = new JSONObject()
            .put("template", new JSONArray()
                .put(new JSONObject()
                    .put("type", "text")
                    .put("content", "Detailed content here")
                    .put("attributes", new JSONObject()
                        .put("size", "lg")
                        .put("color", "#FFFFFF"))));
                        
        activation.showDetail(detailData, () -> activation.hideDetail());
    }
}
```

### JSON Structure

#### Preview Data
```json
{
    "title": "Title text",
    "subtitle": "Subtitle text",
    "backgroundColor": "#000000",
    "backgroundOpacity": 0.66,
    "template": [
        {
            "type": "text",
            "content": "Custom content",
            "attributes": {
                "size": "lg",
                "color": "#FFFFFF"
            }
        }
    ]
}
```

#### Detail Data
```json
{
    "template": [
        {
            "type": "text",
            "content": "Text content",
            "attributes": {
                "size": "lg",
                "color": "#FFFFFF"
            }
        },
        {
            "type": "image",
            "content": "https://example.com/image.jpg",
            "attributes": {
                "width": "100%"
            }
        },
        {
            "type": "row",
            "children": [
                {
                    "type": "button",
                    "content": "Button 1",
                    "attributes": {
                        "width": "50%"
                    }
                },
                {
                    "type": "button",
                    "content": "Button 2",
                    "attributes": {
                        "width": "50%"
                    }
                }
            ]
        }
    ]
}
```

### Supported Segments

1. Text (`"type": "text"`)
   - Supports size, color, weight, style, alignment
   - Size tokens: xxs, xs, sm, md, lg, xl, xxl

2. Image (`"type": "image"`)
   - Supports percentage-based sizing
   - URLs for image content

3. Button (`"type": "button"`)
   - Supports background color, text color, size
   - Percentage-based widths

4. Row (`"type": "row"`)
   - Horizontal layout container
   - Supports child elements
   - Percentage-based widths for children

5. Column (`"type": "column"`)
   - Vertical layout container
   - Supports child elements
   - Percentage-based heights

## License

Copyright © 2025 Source Digital, Inc.

Licensed under the Apache License, Version 2.0