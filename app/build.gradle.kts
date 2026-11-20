plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.freesoul.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.freesoul.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1-test"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

// CI rebuild trigger
