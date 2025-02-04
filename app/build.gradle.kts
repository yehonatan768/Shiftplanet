plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.shiftplanet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shiftplanet"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        debug {
            buildConfigField("int", "ANR_TIMEOUT", "30000")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("int", "ANR_TIMEOUT", "30000")

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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core Dependencies
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BOM (Manages all Firebase dependencies)
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase Services
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")

    // Fix Firestore & ProtoBuf Compatibility
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // UI Testing Dependencies
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.intents.v351)

    // Mockito for Firebase Mocking (Ensure correct version)
    androidTestImplementation(libs.mockito.android)

    // Unit Testing Dependencies
    testImplementation(libs.junit)  // JUnit for unit tests
}
