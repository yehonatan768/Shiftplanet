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
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BOM (Manages all Firebase dependencies)
    implementation(platform(libs.firebase.bom.v3270))


    // Firebase Services
    implementation(libs.com.google.firebase.firebase.auth2)
    implementation(libs.com.google.firebase.firebase.firestore2)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.messaging)
    implementation(libs.com.google.firebase.firebase.analytics)
    implementation(libs.google.firebase.storage)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.firestore)


    // UI Testing Dependencies
    implementation(libs.protobuf.javalite.v3251)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.intents.v351)
    implementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.espresso.core)


    // Unit Testing Dependencies

    androidTestImplementation(libs.mockito.android)
    testImplementation (libs.junit.jupiter.api)
    testImplementation (libs.mockito.core)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation (libs.junit.jupiter.engine)


    configurations.all {
        resolutionStrategy {
            force("com.google.protobuf:protobuf-javalite:3.21.12")  // Adjust if necessary
        }
    }

}
