plugins {
    id("com.android.application")
}

android {
    namespace = "com.org.customvideoplayer"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.org.customvideoplayer"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.11.0")
//    implementation("com.github.bumptech.glide:glide:4.8.0"){
//        exclude("com.android.support", "support-compat")
//    }
    implementation("com.github.bumptech.glide:compiler:4.4.0")
    implementation ("com.google.android.exoplayer:exoplayer:2.18.3")
    implementation ("com.google.code.gson:gson:2.10.1")
}