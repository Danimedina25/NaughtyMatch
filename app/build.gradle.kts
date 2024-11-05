plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")                   // Para Dagger Hilt y Room
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.danifitdev.naughtymatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.danifitdev.naughtymatch"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    implementation(libs.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime.android)// Para iconos extendidos
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.activity:activity-compose:1.7.2") // Integración de Compose con Activity
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Navigation for Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Lifecycle, ViewModel, LiveData, Flow, y otros componentes
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Room Database
    //implementation(libs.androidx.room.runtime)
    //kapt(libs.androidx.room.compiler)
    //implementation(libs.androidx.room.ktx)   // Soporte para corutinas y Flow en Room

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Convertidor JSON usando Gson
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // OkHttp (Cliente HTTP para Retrofit)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    // Hilt core

    // Hilt para Jetpack Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth")
    implementation("com.facebook.android:facebook-android-sdk:[4,5)")
    implementation(libs.facebook.login)

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.code.gson:gson:2.8.9")

}