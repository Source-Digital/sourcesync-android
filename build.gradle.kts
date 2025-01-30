plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    alias(libs.plugins.android.application) apply false
}

android {
    namespace = "io.sourcesync.sdk.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.Source-Digital"
            artifactId = "sourcesync-sdk-ui-android"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}