package com.linreal.recomposition.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

open class RecompositionTrackerExtension {
    var enabled: Boolean = true
    var skipInline: Boolean = true
    var onlyInDebug: Boolean = true
}

@Suppress("unused") // Used via reflection.
class RecompositionTrackerGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        println("[RecompositionTrackerGradlePlugin] Applied to project: ${target.name}")
        target.extensions.create("recompositionTracker", RecompositionTrackerExtension::class.java)

        // Add compiler plugin to classpath (composite build friendly).
        target.dependencies.add(
            "kotlinCompilerPluginClasspath",
            target.rootProject.project(":compiler-plugin:recomposition-tracker:plugin")
        )
        
        // Add runtime dependency based on project type
        addRuntimeDependency(target)
    }
    
    private fun addRuntimeDependency(project: Project) {
        project.afterEvaluate {
            val kmpExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kmpExtension != null) {
                // Multiplatform project - add to androidMain since RecomposeTracker is Android-only for now
                project.dependencies.add(
                    "androidMainImplementation",
                    project.rootProject.project(":compiler-plugin:recomposition-tracker:runtime")
                )
            } else {
                // Regular Android/JVM project
                project.dependencies.add(
                    "implementation",
                    project.rootProject.project(":compiler-plugin:recomposition-tracker:runtime")
                )
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val ext = project.extensions.findByType(RecompositionTrackerExtension::class.java) ?: return false
        if (!ext.enabled) return false

        if (ext.onlyInDebug) {
            val android = project.extensions.findByType(BaseExtension::class.java)
            if (android != null) {
                val compilationName = kotlinCompilation.name.lowercase()
                return compilationName.contains("debug")
            }
        }
        return true
    }

    override fun getCompilerPluginId(): String = "com.linreal.plugin.recomposition-tracker"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.linreal",
        artifactId = "recomposition-tracker-compiler-plugin",
        version = "1.0.0"
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val ext = project.extensions.getByType(RecompositionTrackerExtension::class.java)
        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = ext.enabled.toString()),
                SubpluginOption(key = "skipInline", value = ext.skipInline.toString())
            )
        }
    }
}
