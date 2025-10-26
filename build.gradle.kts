buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.maven.publish) apply false
}

val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Make properties available to root project first
localProperties.forEach { key, value ->
    if (!project.hasProperty(key.toString())) {
        project.ext.set(key.toString(), value)
    }
}

// Make signing properties available to all subprojects
allprojects {
    localProperties.forEach { key, value ->
        if (!project.hasProperty(key.toString())) {
            project.ext.set(key.toString(), value)
        }
    }
}
