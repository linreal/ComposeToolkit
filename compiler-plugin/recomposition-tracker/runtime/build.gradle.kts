plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    // Kotlin Compose compiler plugin
    alias(libs.plugins.kotlin.compose)
    // Compose Multiplatform plugin (prep for common Compose UI if needed)
    alias(libs.plugins.compose)
}

kotlin {
    androidTarget()
    // Optional JVM target so common code can be resolved in non-Android builds
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":compiler-plugin:logger:runtime"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.compose.runtime)
            }
        }
    }
}

android {
    namespace = "com.linreal.recomposition.runtime"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
