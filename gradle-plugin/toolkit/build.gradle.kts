plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    id("io.github.linreal.publishing-conventions")
}

dependencies {
    implementation(project(":gradle-plugin:sub-plugin:recomposition-tracker"))
}

gradlePlugin {
    plugins {
        create("composeToolkitPlugin") {
            id = "io.github.linreal.compose-toolkit"
            implementationClass = "io.github.linreal.toolkit.ComposeToolkitGradlePlugin"
            displayName = "Compose Toolkit"
            description = "Aggregates Compose Toolkit sub-plugins"
        }
    }
}
kotlin {
    jvmToolchain(17)
}
