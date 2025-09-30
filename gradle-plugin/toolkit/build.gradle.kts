plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":gradle-plugin:sub-plugin:logger"))
    implementation(project(":gradle-plugin:sub-plugin:recomposition-tracker"))
}

gradlePlugin {
    plugins {
        create("composeToolkitPlugin") {
            id = "io.github.linreal.compose-toolkit"
            implementationClass = "io.github.linreal.toolkit.ComposeToolkitGradlePlugin"
            version = "1.0.0"
            displayName = "Compose Toolkit"
            description = "Aggregates Compose Toolkit sub-plugins"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

