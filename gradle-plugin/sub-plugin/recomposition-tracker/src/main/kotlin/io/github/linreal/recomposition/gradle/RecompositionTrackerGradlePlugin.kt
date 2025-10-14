package io.github.linreal.recomposition.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.util.concurrent.atomic.AtomicBoolean
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

open class RecompositionTrackerExtension {
    var enabled: Boolean = true
    var onlyInDebug: Boolean = true
}

@Suppress("unused") // Used via reflection.
class RecompositionTrackerGradlePlugin : KotlinCompilerPluginSupportPlugin {

    companion object {
        private const val PLUGIN_GROUP = "io.github.linreal"
        private const val PLUGIN_ARTIFACT = "recomposition-tracker-compiler-plugin"
        private const val PLUGIN_VERSION = "0.1.7-SNAPSHOT"
        private const val RUNTIME_ARTIFACT = "recomposition-tracker-runtime"
    }
    override fun apply(target: Project) {
        println("[RecompositionTrackerGradlePlugin] Applied to project: ${target.name}")
        target.extensions.create("recompositionTracker", RecompositionTrackerExtension::class.java)

        val runtimeDependency = "$PLUGIN_GROUP:$RUNTIME_ARTIFACT:$PLUGIN_VERSION"
        val runtimeAddedToMultiplatform = AtomicBoolean(false)

        // Add compiler plugin to classpath using published coordinates
        target.dependencies.add(
            "kotlinCompilerPluginClasspath",
            "$PLUGIN_GROUP:$PLUGIN_ARTIFACT:$PLUGIN_VERSION"
        )

        target.configurations.matching { it.name == "commonMainImplementation" }
            .all {
                target.dependencies.add(it.name, runtimeDependency)
                runtimeAddedToMultiplatform.set(true)
            }

        target.afterEvaluate {
            if (!runtimeAddedToMultiplatform.get()) {
                target.dependencies.add("implementation", runtimeDependency)
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

    override fun getCompilerPluginId(): String = "io.github.linreal.plugin.recomposition-tracker"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = PLUGIN_GROUP,
        artifactId = PLUGIN_ARTIFACT,
        version = PLUGIN_VERSION
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val ext = project.extensions.getByType(RecompositionTrackerExtension::class.java)
        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = ext.enabled.toString()),
            )
        }
    }
}
