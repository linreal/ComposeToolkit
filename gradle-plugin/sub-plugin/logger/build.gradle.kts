plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":compiler-plugin:logger:plugin"))
    
    // Gradle plugin development
    implementation(libs.kotlin.gradle.plugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("com.android.tools.build:gradle:8.12.1")
}

gradlePlugin {
    plugins {
        create("loggerSubPlugin") {
            id = "com.linreal.logger"
            implementationClass = "com.linreal.logging.gradle.LoggingGradlePlugin"
            version = "1.0.0"
            displayName = "Logger Sub-Plugin"
            description = "Gradle wiring for the logger compiler plugin (@Logging)"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

