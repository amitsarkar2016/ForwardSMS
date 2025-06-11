import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)

    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
//    id("kotlin-kapt")
}

android {
    namespace = "amitsarkar2016.forward.sms"
    compileSdk = 34

    defaultConfig {
        applicationId = "amitsarkar2016.forward.sms"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePathVal = project.findProperty("UPLOAD_KEYSTORE_PATH") as String? ?: return@create
            val storePasswordVal = project.findProperty("KEYSTORE_PASSWORD") as String? ?: ""
            val keyAliasVal = project.findProperty("KEY_ALIAS") as String? ?: ""
            val keyPasswordVal = project.findProperty("KEY_PASSWORD") as String? ?: ""

            storeFile = file(keystorePathVal)
            storePassword = storePasswordVal
            keyAlias = keyAliasVal
            keyPassword = keyPasswordVal

            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            android.applicationVariants.all {
                outputs.all {
                    val date = Date()
                    val formattedDate = SimpleDateFormat("ddmmyyyy-HHmmss", Locale.getDefault()).format(date)
                    (this as BaseVariantOutputImpl).outputFileName = "$applicationId $formattedDate.apk"
                }
            }
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
        viewBinding = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.auth.api.phone)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //unit
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)


    // Dagger-Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.adapter.rxjava2)

    //lifecycle
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //room database
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Recyclerview swipe decoration
    implementation(libs.recyclerview.swipedecorator)

    // Worker
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")
    implementation("com.google.firebase:firebase-messaging:25.0.0")


}