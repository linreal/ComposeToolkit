plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.maven.publish)
}

dependencies {
    implementation(project(":compiler-plugin:logger:plugin"))
    
    // Gradle plugin development
    implementation(libs.kotlin.gradle.plugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("com.android.tools.build:gradle:8.12.1")
}

gradlePlugin {
    plugins {
        create("loggerSubPlugin") {
            id = "io.github.linreal.logger"
            implementationClass = "io.github.linreal.logging.gradle.LoggingGradlePlugin"
            displayName = "Logger Sub-Plugin"
            description = "Gradle wiring for the logger compiler plugin (@Logging)"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral()

    // Only sign if credentials are available
    if (project.hasProperty("signing.keyId")) {
        signAllPublications()
    }

    pom {
        name.set(project.findProperty("POM_NAME")?.toString() ?: project.name)
        description.set(project.findProperty("POM_DESCRIPTION")?.toString())
        inceptionYear.set(project.findProperty("POM_INCEPTION_YEAR")?.toString())
        url.set(project.findProperty("POM_URL")?.toString())

        licenses {
            license {
                name.set(project.findProperty("POM_LICENSE_NAME")?.toString())
                url.set(project.findProperty("POM_LICENSE_URL")?.toString())
                distribution.set(project.findProperty("POM_LICENSE_DIST")?.toString())
            }
        }

        developers {
            developer {
                id.set(project.findProperty("POM_DEVELOPER_ID")?.toString())
                name.set(project.findProperty("POM_DEVELOPER_NAME")?.toString())
                url.set(project.findProperty("POM_DEVELOPER_URL")?.toString())
            }
        }

        scm {
            url.set(project.findProperty("POM_SCM_URL")?.toString())
            connection.set(project.findProperty("POM_SCM_CONNECTION")?.toString())
            developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION")?.toString())
        }
    }
}
