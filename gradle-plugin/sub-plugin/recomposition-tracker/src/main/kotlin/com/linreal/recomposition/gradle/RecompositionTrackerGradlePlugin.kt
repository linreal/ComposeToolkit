package com.linreal.recomposition.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused") // Used via reflection.
class RecompositionTrackerGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        // Extension could be added later as needs emerge.
        println("[RecompositionTrackerGradlePlugin] Applied to project: ${target.name}")
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        // Not applicable until the compiler plugin is implemented/configured.
        return false
    }

    override fun getCompilerPluginId(): String = "com.linreal.plugin.recomposition-tracker"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.linreal",
        artifactId = "recomposition-tracker-compiler-plugin",
        version = "1.0.0"
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }
}

