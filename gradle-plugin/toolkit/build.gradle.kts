plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
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
            displayName = "Compose Toolkit"
            description = "Aggregates Compose Toolkit sub-plugins"
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
