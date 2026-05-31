plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.martiz05.buyapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.martiz05.buyapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
}
