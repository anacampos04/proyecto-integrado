plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.anacampospi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.anacampospi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Leer API key de TMDb desde local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val tmdbApiKey = properties.getProperty("tmdb.api.key") ?: "YOUR_API_KEY_HERE"
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Retrofit para llamadas HTTP a TMDb API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil para carga de imágenes (posters)
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    // Import the Firebase BoM
    // BoM actual
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    // (Opcional) si vas a usar Auth o Analytics:
    implementation("com.google.firebase:firebase-auth")
    //implementation("com.google.firebase:firebase-analytics")

    // Firebase Cloud Messaging para notificaciones push
    implementation("com.google.firebase:firebase-messaging")

    //iconos animados
    implementation("com.airbnb.android:lottie-compose:6.0.0")

    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}