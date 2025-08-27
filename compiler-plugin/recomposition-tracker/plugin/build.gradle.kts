plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
}

kotlin {
    jvmToolchain(17)
}

