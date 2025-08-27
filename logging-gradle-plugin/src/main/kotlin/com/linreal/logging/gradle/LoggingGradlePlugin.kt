package com.linreal.logging.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused") // Used via reflection.
class LoggingGradlePlugin : KotlinCompilerPluginSupportPlugin {
    
    override fun apply(target: Project) {
        println("[LoggingGradlePlugin] Applied to project: ${target.name}")
        target.extensions.create("logging", LoggingExtension::class.java)
        
        // Add compiler plugin to classpath for composite build
        target.dependencies.add(
            "kotlinCompilerPluginClasspath",
            target.rootProject.project(":logging-compiler-plugin")
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(LoggingExtension::class.java) 
            ?: return false
            
        println("[LoggingGradlePlugin] Checking applicability for compilation: ${kotlinCompilation.name}")
        println("[LoggingGradlePlugin] Extension enabled: ${extension.enabled}")
            
        if (!extension.enabled) return false
        
        // Check if this is a debug build when onlyInDebug is true
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

    override fun getCompilerPluginId(): String = "com.linreal.plugin.logging"

    override fun getPluginArtifact(): SubpluginArtifact {
        // For composite build/local development
        return SubpluginArtifact(
            groupId = "com.linreal", 
            artifactId = "logging-compiler-plugin", 
            version = "1.0.0"
        )
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(LoggingExtension::class.java)
        
        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = extension.enabled.toString()),
                SubpluginOption(key = "skipInline", value = extension.skipInline.toString())
            )
        }
    }
}