plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.googlelens"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.googlelens"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    // Google's Image labeling ML Kit
    implementation("com.google.mlkit:image-labeling:17.0.8")

// adding Volley library for API calling
    implementation("com.android.volley:volley:1.2.1")

// below line is used for image loading library
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}