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
        versionCode = 1
        versionName = "0.1.0-test"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
