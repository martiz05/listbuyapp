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
        buildConfigField("String", "BUYAPP_API_BASE_URL", "\"https://api.example.invalid\"")
        manifestPlaceholders["buyAppUsesCleartextTraffic"] = "false"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BUYAPP_API_BASE_URL", "\"http://10.0.2.2:5130\"")
            manifestPlaceholders["buyAppUsesCleartextTraffic"] = "true"
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
}
