package io.github.linreal.logging.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused") // Used via reflection.
class LoggingGradlePlugin : KotlinCompilerPluginSupportPlugin {

    companion object {
        private const val PLUGIN_GROUP = "io.github.linreal"
        private const val PLUGIN_ARTIFACT = "logger-compiler-plugin"
        private const val PLUGIN_VERSION = "0.1.5-SNAPSHOT"
        private const val ANNOTATIONS_ARTIFACT = "logging-annotations"
    }
    
    override fun apply(target: Project) {
        // Create the user-facing extension: project.logging { ... }
        println("[LoggingGradlePlugin] Applied to project: ${target.name}")
        target.extensions.create("logging", LoggingExtension::class.java)

        // Add compiler plugin to classpath using published coordinates
        target.dependencies.add(
            "kotlinCompilerPluginClasspath",
            "$PLUGIN_GROUP:$PLUGIN_ARTIFACT:$PLUGIN_VERSION"
        )

        // Add runtime annotations to implementation classpath
        target.dependencies.add(
            "implementation",
            "$PLUGIN_GROUP:$ANNOTATIONS_ARTIFACT:$PLUGIN_VERSION"
        )
    }


    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(LoggingExtension::class.java) 
            ?: return false
            
        println("[LoggingGradlePlugin] Checking applicability for compilation: ${kotlinCompilation.name}")
        println("[LoggingGradlePlugin] Extension enabled: ${extension.enabled}")
            
        if (!extension.enabled) return false
        
        // Apply only to debug variants when requested and Android plugin is present.
        if (extension.onlyInDebug) {
            val android = project.extensions.findByType(BaseExtension::class.java)
            if (android != null) {
                // For Android projects, check if this is a debug variant
                val compilationName = kotlinCompilation.name.lowercase()
                val isDebug = compilationName.contains("debug")
                println("[LoggingGradlePlugin] OnlyInDebug=true, compilation=$compilationName, isDebug=$isDebug")
                return isDebug
            }
        }
        
        println("[LoggingGradlePlugin] Plugin is applicable for ${kotlinCompilation.name}")
        return true
    }

    /** The compiler-side plugin id. Must match registrar's `PLUGIN_ID`. */
    override fun getCompilerPluginId(): String = "io.github.linreal.plugin.logging"

    /**
     * Points Gradle to the compiler plugin artifact. In composite builds this
     * resolves to the included build module `:compiler-plugin:logger:plugin`.
     */
    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = PLUGIN_GROUP,
            artifactId = PLUGIN_ARTIFACT,
            version = PLUGIN_VERSION
        )
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(LoggingExtension::class.java)
        
        // Map extension values into options the compiler plugin understands.
        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = extension.enabled.toString()),
                SubpluginOption(key = "skipInline", value = extension.skipInline.toString())
            )
        }
    }
}

