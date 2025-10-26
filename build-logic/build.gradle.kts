plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.34.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
}

gradlePlugin {
    plugins {
        register("publishingConventions") {
            id = "io.github.linreal.publishing-conventions"
            implementationClass = "io.github.linreal.gradle.PublishingConventionsPlugin"
        }
    }
}
