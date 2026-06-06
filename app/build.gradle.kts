plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// CHIẾN LƯỢC DỨT ĐIỂM LỖI ANDROID 15/16: 
// Ép toàn bộ project sử dụng bản vá lỗi IInputManager của Google.
// Đặt khối này ở ngoài cùng để có quyền ưu tiên cao nhất.
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.test.espresso") {
            useVersion("3.6.1")
        }
        if (requested.group == "androidx.test" || requested.group == "androidx.test.services") {
            if (requested.name == "monitor") useVersion("1.7.2")
            else if (requested.name == "runner") useVersion("1.6.2")
            else if (requested.name == "rules") useVersion("1.6.1")
            else if (requested.name == "core") useVersion("1.6.1")
        }
        if (requested.group == "androidx.test.ext" && requested.name == "junit") {
            useVersion("1.2.1")
        }
    }
}

android {
    namespace = "com.infix.gamelatthe"
    compileSdk = 36

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
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
    // UI & Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    
    // Lifecycle & Navigation
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.fragment)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Glide & Room
    implementation(libs.glide)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // UNIT TESTING
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.core.testing)
    testImplementation(libs.room.testing)

    // INSTRUMENTED TESTING (Ép dùng bản chuẩn cho Android 15/16)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:monitor:1.7.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation(libs.core.testing)
    
    // Quan trọng: debugImplementation giúp FragmentScenario hoạt động đúng
    debugImplementation(libs.fragment.testing)
}
