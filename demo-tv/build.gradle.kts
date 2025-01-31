plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.sourcesync.sdk.ui.demo_tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.sourcesync.sdk.ui.demo_tv"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.androidx.leanback)
    implementation(libs.glide)
}