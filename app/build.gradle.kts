import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Imza bilgileri local.keystore.properties dosyasindan okunur (repo disi, .gitignore'da):
//   storeFile=/Users/zeynep/Desktop/Android Projeler/vakitvedua/Untitled.jks
//   storePassword=...
//   keyAlias=...
//   keyPassword=...
// Dosya yoksa release imzasi bos kalir; CI GitHub Actions secrets ile jarsigner imzalar.
val keystoreProps = Properties().apply {
    val f = rootProject.file("local.keystore.properties")
    if (f.exists()) FileInputStream(f).use { load(it) }
}

val releaseStoreFile: File? = run {
    val configured = keystoreProps.getProperty("storeFile")
    when {
        !configured.isNullOrBlank() -> rootProject.file(configured)
        else -> null
    }
}
val releaseStorePassword = keystoreProps.getProperty("storePassword")
val releaseKeyAlias = keystoreProps.getProperty("keyAlias")
val releaseKeyPassword = keystoreProps.getProperty("keyPassword")
val hasReleaseKeystore = releaseStoreFile != null &&
        !releaseStorePassword.isNullOrBlank() && !releaseKeyAlias.isNullOrBlank()

android {
    namespace = "com.stitchilyas.vakitvedua"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.stitchilyas.vakitvedua"
        minSdk = 26
        targetSdk = 36
        versionCode = 15500
        versionName = "1.55.00"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            val debugKeystore = file("${rootDir}/debug.keystore")
            if (debugKeystore.exists()) {
                storeFile = debugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword ?: releaseStorePassword
            }
            // Anahtar bulunamazsa bos birakilir: CI AAB'i imzasiz uretir ve
            // ANDROID_KEYSTORE_* secrets ile jarsigner ile imzalar.
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Anahtar yerelde varsa imzalı, yoksa imzasız üretilir;
            // CI GitHub Secrets ile jarsigner imzasını tamamlar.
            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("release") else null
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
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
