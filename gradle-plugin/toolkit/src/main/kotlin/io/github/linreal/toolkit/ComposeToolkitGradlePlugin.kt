package io.github.linreal.toolkit

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Main Gradle plugin that aggregates individual sub-plugins, so users can
 * apply a single plugin id: `io.github.linreal.compose-toolkit`.
 */
class ComposeToolkitGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("io.github.linreal.recomposition-tracker")
    }
}
