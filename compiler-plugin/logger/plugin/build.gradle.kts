plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":logging-annotations"))
    compileOnly(libs.kotlin.compiler.embeddable)
}

kotlin {
    jvmToolchain(17)
}

