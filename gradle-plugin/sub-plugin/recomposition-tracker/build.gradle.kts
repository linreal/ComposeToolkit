plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.maven.publish)
    id("io.github.linreal.publishing-conventions")
}

dependencies {
    implementation(project(":compiler-plugin:recomposition-tracker:plugin"))
    // Gradle plugin development only.
    implementation(libs.kotlin.gradle.plugin.api)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle)
}

gradlePlugin {
    plugins {
        create("recompositionTrackerSubPlugin") {
            id = "io.github.linreal.recomposition-tracker"
            implementationClass = "io.github.linreal.recomposition.gradle.RecompositionTrackerGradlePlugin"
            displayName = "Recomposition Tracker Sub-Plugin"
            description = "Gradle wiring for a future recomposition-tracker compiler plugin"
        }
    }
}
kotlin {
    jvmToolchain(17)
}
