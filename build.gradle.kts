plugins {
    alias(libs.plugins.android.library) version "8.2.0"
    id("maven-publish")
}

android {
    namespace = "io.sourcesync.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 16

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.networknt:json-schema-validator:1.0.72")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.Source-Digital.native-sdk"
            artifactId = "sourcesync-android"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
