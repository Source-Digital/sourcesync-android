# SourceSync Android SDK

## Overview
SourceSync is a platform that syncronizes metadata with content for almost any reason. Monetize content, educate your viewers, sell related merchandise, provide trivia, additional content, you name it. Monetize or otherwise support your data with dozens of additional layers of information, autmatically, including:

1. Transcripts.
2. Descriptions of what's on screen.
3. Mood and sentiment data.
4. Categories contained within the moment including advertizing (IAB v1, v2 and v3 supported), nouns and moure.
5. Keywords contained within the moment, including people, places, things, goals, scores, underdogs, and more.
6. Entities within content, including Wikipedia IDs, Google Knolwedge Graph IDs, IMDB IDs and more.

The Android SDK is a native implementation that enables SourceSync within your app. It allows synchronized overlay content to be displayed on top of video content. The system supports multiple overlay positions with independent content streams, making it possible to show different content at different positions simultaneously.

## Installation

### Step 1: Add JitPack repository
Add JitPack repository in your root build.gradle at the end of repositories:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the dependency
Add the dependency to your app's build.gradle:

```gradle
dependencies {
    implementation 'com.github.Source-Digital.native-sdk:sourcesync-android:v0.0.1'
}
```

## Quick Start

### Get an API key
Like most APIs, SourceSync requires an API key for usage. This key dictates what you have access to, your settings and more. Get one by visiting the [developer portal](https://developer.sourcesync.io), giving yourself one by logging into the admin app ["Kurator"](https://admin.sourcesync.io) (under your account page, then the organization page, then the access tab), or to quickly kick the tires, just use our demo key, which is ```app.v1.demo```

### Initialize SDK
```java
// Initialize the SDK with your API key
SourceSync sourcesync = SourceSync.setup(context, "your-api-key");
```

### Load a Distribution
A ```distribution``` is content that has been curated specifically for SourceSync. In addition to just content we process for metadata, a distribution has even more to offer, like specific access restrictions, analytics, and optional manual or automatically added ```activations``` that are time-aligned for your audience.

```java
// Load a content distribution
Distribution distribution = sourcesync.getDistribution("distribution-id");
```
 ### Plop it on screen
 ```java
// Create overlay containers for all positions
Map<String, View> overlays = sourcesync.createPositionedOverlays(
    distribution, 
    videoView, 
    "top", "bottom", "left", "right"
);

// Add overlays to your layout
layout.addView(overlays.get("top"));
layout.addView(overlays.get("bottom"));
layout.addView(overlays.get("left"));
layout.addView(overlays.get("right"));
```
That's it! Activations will now show in your app in the places you want!

# Diving deeper

### What is context?
In Android, ```context``` is an interface to global informaiton about the app environement. It's typically an Activity or Application instance that provides access to resources, system services, and/or application-level operations. SourceSync will use this to automatically syncronize to your video and help you easily setup overlays in your application.

```java
// In an Activity
SourceSync.setup(this, "your-api-key");

// OR using application context
SourceSync.setup(getApplicationContext(), "your-api-key");

// OR from a Fragment
SourceSync.setup(requireContext(), "your-api-key");
```

# Settings
SourceSync revolves around settings. They can be changed in real-time and can totally reconfigure the entire behavior of the application in any way you can imagine.

### Customizing settings

Optionally, you can customize anything the SDK does by passing settings like this:

```java
// Initialize the SDK with your API key and your settings overrides
SourceSync sourcesync = SourceSync.setup(context, "your-api-key", settings);
```

Where ```settings``` is just a JSON object that follows our settings schema.

### Get settings in realtime
Don't want to hard-code your settings in your app? We already took care of that for you! You can provide settings right within your API Key. From [Kurator](https://admin.sourcesync.io), go to your account page, then organization, then access, they create a new key or tap an existing key. Click on the settings tab. You can create your custom settings here. Whatever you enter will be applied to every subsequnt access to our platform, instantly. In this way, you can update activation previews, colors, text sizes, you name it, reconfiguring your app on the fly - reprogram your app from the key alone! Issue different versions of your app by just issuing different keys to your customers!

Automatically, and by default, SourceSync will apply your key settings on top of our default settings. So just simply setting them for your key is all you need to do, you don't even need to provide a settings object in your app. **this is all provided by default**.

### Use SourceSync for your own settings
Like our realtime settings feature and want to configure your whole app like that? No problem! We have a special "value" key section we return where you can place *anything*. Use this space to configure your app in any way you see fit! While there is no hard-limitation on the size of the data (you could use 20mb or more here), we recommend you use 64kb or less for practical reasons (the time it takes to load your key/etc).

# Overlays
An ```overlay``` is a place within your application that can automatically recieve ```activations```. Use them to get both notified that activations and metadata is available, as well as have full control of the display it natively takes within your application.

Overlays can be mapped to named positions called, well, ```positions```. Although you can name any position you want, most templates will include the 4 basic positional names of "top", "bottom", "left" and "right". While you can map any target to any overlay in your app you'd like, the names can help you understand the original intention of the person that curated the content without specifically coordinating with them.

For example, if the person curated content for an L-Bar type of overlay for CTV, and you want to show the content on a Mobile device in portrait mode under your video, simply map all overlay targets to one single overlay you have under your video.

However, if you notice minor data like scoring and ticker information always targets "bottom", you could simply make a special overlay just for that, and show it however you want to.

So for example...
```java
FrameLayout mainLayout = findViewById(R.id.main_layout);
VideoView videoView = findViewById(R.id.video_view);

// SourceSync provides the overlays map to your app...
Map<String, View> overlays = sourcesync.createPositionedOverlays(
    distribution,
    videoView,
    "top", "bottom"
);

// You then add overlay targets to your layout...

mainLayout.addView(overlays.get("top"));    // Places overlay at top
mainLayout.addView(overlays.get("bottom")); // Places overlay at bottom
```

That's it!

You can also fully control ```activations```. Activations are what is put into your overlays. You can even create your own overlays with 

## Requirements
- Minimum SDK: Android 24 (Android 7.0)
- AndroidX
- Java 11

## Dependencies
The SDK uses the following dependencies:
- androidx.appcompat:appcompat:1.6.1
- com.fasterxml.jackson.core:jackson-databind:2.14.2
- com.networknt:json-schema-validator:1.0.72

## Features
- Multiple overlay positions support
- Automatic content synchronization with video playback
- Dynamic content loading
- Transition effects
- Customizable templates
- Preview and detail view modes

## Content Structure

### Distribution
Represents a content distribution configuration containing:
- Distribution metadata
- List of activation instances
- Time window definitions

### Activation
Represents a single piece of content that can be displayed:
- Content metadata
- Display settings
- Template definition
- Visual content data

## Best Practices
- Initialize SDK early in your application lifecycle
- Handle back button events for detail view mode
- Properly dispose of resources when no longer needed
- Monitor video playback state changes

## Example Usage

Here's a complete example of how to integrate the SDK:

```java
public class VideoActivity extends AppCompatActivity {
    private SourceSync sourcesync;
    private VideoView videoView;
    private FrameLayout overlayContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Initialize SDK
        sourcesync = SourceSync.setup(this, "your-api-key");

        // Set up video view
        videoView = findViewById(R.id.video_view);
        overlayContainer = findViewById(R.id.overlay_container);

        // Load distribution
        try {
            Distribution distribution = sourcesync.getDistribution("your-distribution-id");

            // Create overlays
            Map<String, View> overlays = sourcesync.createPositionedOverlays(
                distribution,
                videoView,
                "top", "bottom"
            );

            // Add overlays to container
            overlayContainer.addView(overlays.get("top"));
            overlayContainer.addView(overlays.get("bottom"));

        } catch (IOException | JSONException e) {
            Log.e("VideoActivity", "Error setting up SourceSync", e);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button for detail view
        if (!sourcesync.handleBackButton()) {
            super.onBackPressed();
        }
    }
}
```

# Creating Activations
You can use our UI to create activations. Just login to [Kurator](https://admin.sourcesync.io) and click ```activations```.

You can also create your own activations programmatically right from the SDK! All you need is a JSON object, which you can store in your own services, in our services, in your app itself, etc. The JSON is very simple but robust. Here is what a minimal activation looks like...

```jsonc
{
  "id": 1, // Arbitrary, but needed for your app to tell which activation it's being told to render. This is also sent to your anaytics and logs.
  "name": "Your activation name", // Also arbitrary, but needed for your app and analytics.
  "instances": [
    {
        "when": {
            "start": 1000,  // time in ms
            "end": 5000     // time in ms
        }
    }
  ]
}
```
That's all you need!

The above will render an activation with id 1 at 1 second to 5 seconds. 

The above activation will use many default settings for everything since we didn't fill anything out.

There are many more items you can add, besides the minimum...

## Additional settings

```jsonc
{
  "id": 1,
  "name": "Your activation name",
  "instances": [
    // The first time it shows up...
    {
        "when": {
            "start": 1000,
            "end": 5000
        }
    },
    // The second time, etc...
    {
        "when": {
            "start": 10000,
            "end": 15000
        }
    },

  ],
  "settings": {
    "preview": {
      "title": "My Title",                    // Title text
      "subtitle": "My Subtitle",              // Subtitle text
      "titleSize": "lg",                      // Size token: xxs, xs, sm, md, lg, xl, xxl
      "subtitleSize": "md",                   // Size token: xxs, xs, sm, md, lg, xl, xxl
      "image": "https://...",                 // Image URL
      "showFomo": true,                       // Show/hide FOMO indicator
      "showImage": true,                      // Show/hide image
      "backgroundAppearance": "imageAndText", // Options: imageAndText, textOnly, imageOnly
      "backgroundColor": "#000000",           // Background color
      "backgroundOpacity": 0.66,               // 0.0 to 1.0
      "template": {
        // ... you can use a block based native render template of any sort here.
      }
    }
  },
  "template": {
    // ... the same block based native render template format goes here. This is where you would place what is shown when the user clicks details
  }
}
```

# Activation defaults
In addition to setting an activation data, you can set activation defaults, so if an activation doesn't have a subtitle, you can add a default one, or if it doesn't specify a preview color, ect.

## Default content
By default, if an activation doesn't fill something out, these defaults will be used.

To set defaults in your settings....

```jsonc
{
    "settings": {
        "preview": {
            "defaults": {
                "default": {
                    "title": "Hey there!",
                    "subtitle": "Tap here for details"
                    // ... etc.
                }
            }
        }
    }
}
```

## Default template

You can also create activation preview template defaults, like this:
```jsonc
{
    "settings": {
        "preview": {
            "defaults": {
                "template": [{
                    "type": "text",
                    "content": "{title}", // variable
                    "attributes": {
                    "size": "lg",
                    "color": "#FFFFFF"
                    }
                }]
            }
        }
    }
}
```

## Template variables

Any preview setting that can be set can also be used in a template. They are:
* {title} - text
* {subtitle} - any size token
* {titleSize} - text
* {subtitleSize} - any size token
* {image} - any valid url
* {showImage} - boolean - show an image in the preview or not
* {showFomo} - boolean - show the activation countdown ring
* {backgroundAppearance} - Use this to define your own
* {backgroundColor} - #RRGGBB format
* {backgroundOpacity} - A number between 0 and 1


## Displaying your own programmatic activation 
If you want, you can construct activations in realtime, for any reason...

### Method 1 - Without a time period (if you want to set this later)
```java
// Step 1: Create JSON WITHOUT an instance array...
JSONObject activationJson = new JSONObject("""
{
  "id": 1,
  "name": "Custom Activation",
  "settings": {
    "preview": {
      "template": [{
        "type": "text",
        "content": "Hello World",
        "attributes": {
          "size": "lg",
          "color": "#FFFFFF"
        }
      }]
    }
  }
}
""");

// Step 2: Create an activation from the JSON...
Activation activation = Activation.fromJson(activationJson);

// Step 3: Create an activation instance WITH a list of time periods...
Distribution.ActivationInstance instance = new Distribution.ActivationInstance(1, 
    Collections.singletonList(new Distribution.TimeWindow(1000, 5000, new JSONObject())));

// Step 4: Add it to your distribution...
distribution.activations.add(instance);
```
### Method 2 - With a time period (If you just want to add it immediately and know when you want it to be displayed)

```java
// Step 1: Create JSON WITH an instance array...
JSONObject activationJson = new JSONObject("""
{
  "id": 1,
  "name": "Custom Activation",
  "settings": {
    "preview": {
      "template": [{
        "type": "text",
        "content": "Hello World",
        "attributes": {
          "size": "lg",
          "color": "#FFFFFF"
        }
      }]
    }
  },
  "instances": [{
    "when": {
      "start": 1000,
      "end": 5000
    },
    "settings": {}
  }]
}
""");

// Step 2: Create an activation from the JSON...
Activation activation = Activation.fromJson(activationJson);

// Step 3: Create an activation instance WITHOUT time periods...
Distribution.ActivationInstance instance = ActivationInstance.fromJson(activationJson);

// Step 4: Add it to your distribution...
distribution.activations.add(instance);
```

## License

Copyright &copy; 2025 Source Digital, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

You may use this software however you see fit as long as, if modified, you contribute modifications back to the community.

## Support
For support, please contact [dev@sourcedigital.net](mailto:dev@sourcedigital.net).