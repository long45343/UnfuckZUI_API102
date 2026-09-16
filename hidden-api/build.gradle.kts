plugins {
    id("com.android.library")
}

android {
    namespace = "xyz.cirno.hidden_api"
    compileSdk = 35

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
