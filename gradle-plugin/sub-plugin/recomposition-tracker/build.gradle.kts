plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

dependencies {
    // Gradle plugin development only; compiler plugin will be added later.
    implementation(libs.kotlin.gradle.plugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
}

gradlePlugin {
    plugins {
        create("recompositionTrackerSubPlugin") {
            id = "com.linreal.recomposition-tracker"
            implementationClass = "com.linreal.recomposition.gradle.RecompositionTrackerGradlePlugin"
            version = "1.0.0"
            displayName = "Recomposition Tracker Sub-Plugin"
            description = "Gradle wiring for a future recomposition-tracker compiler plugin"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

