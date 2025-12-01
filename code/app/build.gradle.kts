import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.slices"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    defaultConfig {
        applicationId = "com.example.slices"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Read Google Maps API key from local.properties
        val mapsApiKey: String = project.findProperty("MAPS_API_KEY") as String? ?: "YOUR_GOOGLE_MAPS_API_KEY_HERE"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    packaging {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "google/protobuf/field_mask.proto"
            )
        }
    }



}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.firebase.firestore)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.location)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.quicktheories:quicktheories:0.26")
    testImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")

    // FragmentScenario (for fragment-only tests)
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    // Navigation testing (to test your adapter + nav graph)
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")

    // Test runner + rules
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    androidTestImplementation("org.mockito:mockito-android:5.10.0")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    //implementation("com.google.protobuf:protobuf-java:3.25.3")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.guava:guava:31.1-android")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("com.google.zxing:core:3.5.4")
}