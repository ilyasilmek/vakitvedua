plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.stitchilyas.vakitvedua"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.stitchilyas.vakitvedua"
        minSdk = 26
        targetSdk = 36
        versionCode = 15404
        versionName = "1.54.04"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val rootKeystore = file("${rootDir}/debug.keystore")
        if (rootKeystore.exists()) {
            getByName("debug") {
                storeFile = rootKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
            create("release") {
                storeFile = rootKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val relConfig = signingConfigs.findByName("release")
            if (relConfig != null) {
                signingConfig = relConfig
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            val dbgConfig = signingConfigs.findByName("debug")
            if (dbgConfig?.storeFile != null) {
                signingConfig = dbgConfig
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.ads)

    debugImplementation(libs.androidx.ui.tooling)
}
