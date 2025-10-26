package io.github.linreal.gradle

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("Unused")
class PublishingConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureProjectVersion()
        pluginManager.withPlugin("com.vanniktech.maven.publish") {
            configurePublishing()
        }
        configureGradlePluginSupport()
    }

    private fun Project.configurePublishing() {
        val publishing = extensions.findByType(MavenPublishBaseExtension::class.java) ?: return

        with(publishing) {
            publishToMavenCentral()
            pom {
                name.set(findStringProperty("POM_NAME") ?: this@configurePublishing.name)
                description.set(findStringProperty("POM_DESCRIPTION"))
                inceptionYear.set(findStringProperty("POM_INCEPTION_YEAR"))
                url.set(findStringProperty("POM_URL"))

                licenses {
                    license {
                        name.set(findStringProperty("POM_LICENSE_NAME"))
                        url.set(findStringProperty("POM_LICENSE_URL"))
                        distribution.set(findStringProperty("POM_LICENSE_DIST"))
                    }
                }

                developers {
                    developer {
                        id.set(findStringProperty("POM_DEVELOPER_ID"))
                        name.set(findStringProperty("POM_DEVELOPER_NAME"))
                        url.set(findStringProperty("POM_DEVELOPER_URL"))
                    }
                }

                scm {
                    url.set(findStringProperty("POM_SCM_URL"))
                    connection.set(findStringProperty("POM_SCM_CONNECTION"))
                    developerConnection.set(findStringProperty("POM_SCM_DEV_CONNECTION"))
                }
            }
        }

        afterEvaluate {
            val shouldSign = hasProperty("signing.keyId")
            if (shouldSign) {
                extensions.findByType(MavenPublishBaseExtension::class.java)
                    ?.signAllPublications()
            }
        }
    }

    private fun Project.configureProjectVersion() {
        val versionName = providers.gradleProperty("VERSION_NAME")
        if (versionName.isPresent) {
            version = versionName.get()
        }
    }

    private fun Project.configureGradlePluginSupport() {
        pluginManager.withPlugin("java-gradle-plugin") {
            val versionName = providers.gradleProperty("VERSION_NAME")
            if (!versionName.isPresent) return@withPlugin

            val generatedDir = layout.buildDirectory.dir("generated/source/publishingConventions/kotlin")
            val generateVersionTask = tasks.register("generatePublishingConventionsVersion") {
                inputs.property("pluginVersion", versionName)
                outputs.dir(generatedDir)

                doLast {
                    val versionValue = versionName.get()
                    val outputDir = generatedDir.get().asFile
                    val packageDir = outputDir.resolve("io/github/linreal/gradle")
                    packageDir.mkdirs()
                    val versionFile = packageDir.resolve("PublishingConventionsVersion.kt")
                    versionFile.writeText(
                        """
                            package io.github.linreal.gradle

                            internal object PublishingConventionsVersion {
                                const val VERSION: String = "$versionValue"
                            }
                        """.trimIndent()
                    )
                }
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                dependsOn(generateVersionTask)
            }

            tasks.withType(Jar::class.java).configureEach {
                if (name == "sourcesJar") {
                    dependsOn(generateVersionTask)
                }
            }

            extensions.findByType(SourceSetContainer::class.java)
                ?.named("main") {
                    java.srcDir(generatedDir)
                }
        }
    }


    private fun Project.findStringProperty(name: String): String? = findProperty(name)?.toString()
}
