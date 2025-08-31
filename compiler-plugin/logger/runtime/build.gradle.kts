plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    jvm()
    
    sourceSets {
        commonMain {
            dependencies {
                // No external dependencies needed
            }
        }
        
        androidMain {
            dependencies {
                // Android-specific dependencies if needed
            }
        }
        
        jvmMain {
            dependencies {
                // JVM-specific dependencies if needed
            }
        }
    }
}

android {
    namespace = "com.linreal.logging.runtime"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

