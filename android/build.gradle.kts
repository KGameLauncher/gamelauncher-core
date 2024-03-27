plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "de.dasbabypixel.gamelauncher"
    compileSdk = 34

    defaultConfig {
        multiDexEnabled = true
        applicationId = "de.dasbabypixel.gamelauncher"
        minSdk = 1
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(libs.androidx.appcompat)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}