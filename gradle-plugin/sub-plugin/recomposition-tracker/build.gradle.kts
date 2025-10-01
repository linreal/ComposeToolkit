plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.maven.publish)
}

dependencies {
    implementation(project(":compiler-plugin:recomposition-tracker:plugin"))
    // Gradle plugin development only.
    implementation(libs.kotlin.gradle.plugin.api)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("com.android.tools.build:gradle:8.12.1")
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
