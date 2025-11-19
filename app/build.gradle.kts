plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplication3"
    compileSdk = 34
    

    defaultConfig {
        applicationId = "com.example.myapplication3"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    // 自定义APK文件名
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val fileName = output.outputFileName
            if (fileName.contains("release") && !fileName.contains("unsigned")) {
                output.outputFileName = "shengcun-${versionName}.apk"
            }
        }
    }
}

dependencies {
    configurations.all {
        resolutionStrategy {
            force("androidx.activity:activity:1.7.2")
            // 排除导致DEX错误的error_prone_annotations依赖
            exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        }
    }

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.7.2")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.13.2")
}