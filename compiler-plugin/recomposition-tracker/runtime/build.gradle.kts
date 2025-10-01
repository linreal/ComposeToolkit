plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "io.github.linreal.recomposition.runtime"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
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
