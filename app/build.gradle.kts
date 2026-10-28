plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.socialnetwork"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.socialnetwork"
        minSdk = 21
        targetSdk = 34
        versionCode = 3
        versionName = "1.1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
