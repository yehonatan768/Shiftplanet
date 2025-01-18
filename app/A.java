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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase BOM
    implementation platform("com.google.firebase:firebase-bom:31.0.0")  // גרסה עשויה להשתנות

    // Firebase libraries (מבלי לציין גרסאות, מכיוון שה-BOM דואג לכך)
    implementation "com.google.firebase:firebase-auth"
    implementation "com.google.firebase:firebase-firestore"
    implementation "com.google.firebase:firebase-database"
    implementation "com.google.firebase:firebase-messaging"  // הוספת firebase-messaging
    implementation "com.google.firebase:firebase-analytics"

    // שירותים אחרים של Google
    implementation "com.google.android.gms:play-services-auth"

    // תלויות נוספות
    implementation "androidx.core:core-ktx"
    implementation "androidx.appcompat:appcompat"
    implementation "com.google.android.material:material"
    implementation "androidx.activity:activity-ktx"
    implementation "androidx.constraintlayout:constraintlayout"

    // בדיקות
    testImplementation "junit:junit"
    androidTestImplementation "androidx.test.ext:junit"
    androidTestImplementation "androidx.test.espresso:espresso-core"
}
