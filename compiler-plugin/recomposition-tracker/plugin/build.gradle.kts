plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    id("io.github.linreal.publishing-conventions")
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
}

kotlin {
    jvmToolchain(17)
}
