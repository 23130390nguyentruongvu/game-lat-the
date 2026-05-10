plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.infix.gamelatthe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.infix.gamelatthe"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures { viewBinding = true }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))

    implementation(libs.glide)
    implementation(libs.room.runtime)
    implementation(libs.fragment)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    testImplementation(libs.room.testing)
    annotationProcessor(libs.room.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}