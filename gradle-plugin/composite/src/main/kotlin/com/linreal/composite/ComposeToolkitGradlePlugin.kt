package com.linreal.composite

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Composite Gradle plugin that aggregates individual sub-plugins, so users can
 * apply a single plugin id: `com.linreal.compose-toolkit`.
 */
class ComposeToolkitGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Apply sub-plugins by id; users can configure them individually.
        target.pluginManager.apply("com.linreal.logger")
        target.pluginManager.apply("com.linreal.recomposition-tracker")
    }
}
