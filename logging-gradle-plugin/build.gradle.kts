plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":compiler-plugin:logging-compiler-plugin"))
    
    // Gradle plugin development
    implementation(libs.kotlin.gradle.plugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("com.android.tools.build:gradle:8.12.1")
}

gradlePlugin {
    plugins {
        create("loggingPlugin") {
            id = "com.linreal.plugin.logging"
            implementationClass = "com.linreal.logging.gradle.LoggingGradlePlugin"
            version = "1.0.0"
            displayName = "Logging Plugin"
            description = "Kotlin compiler plugin that adds automatic logging to functions annotated with @Logging"
        }
    }
}

kotlin {
    jvmToolchain(17)
}