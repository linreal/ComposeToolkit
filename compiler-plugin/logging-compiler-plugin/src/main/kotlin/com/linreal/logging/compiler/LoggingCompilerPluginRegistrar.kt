package com.linreal.logging.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
/**
 * Entry point for the Kotlin compiler (K2) to discover and register our IR extension.
 *
 * - `supportsK2 = true` declares K2 compatibility.
 * - Reads configuration passed from Gradle.
 * - Registers [LoggingIrGenerationExtension] when enabled.
 * - Emits diagnostic messages via [MessageCollector] to help debugging.
 */
class LoggingCompilerPluginRegistrar : CompilerPluginRegistrar() {
    
    override val supportsK2: Boolean = true
    private fun CompilerConfiguration.msg(): MessageCollector =
        get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val mc = configuration.msg()

        mc.report(CompilerMessageSeverity.WARNING, "[LoggingPlugin] registrar entered")
        val enabled = configuration.get(KEY_ENABLED, true)
        val skipInline = configuration.get(KEY_SKIP_INLINE, true)

        mc.report(CompilerMessageSeverity.WARNING, "[LoggingPlugin] enabled=$enabled, skipInline=$skipInline")
        
        if (enabled) {
            IrGenerationExtension.registerExtension(
                LoggingIrGenerationExtension(skipInline)
            )
            mc.report(CompilerMessageSeverity.WARNING, "[LoggingPlugin] IR extension registered")
        }
    }

    companion object {
        const val PLUGIN_ID = "com.linreal.plugin.logging"
        
        /** Compiler configuration key toggling the plugin. */
        val KEY_ENABLED = CompilerConfigurationKey<Boolean>("enabled")
        /** Compiler configuration key to skip inline functions. */
        val KEY_SKIP_INLINE = CompilerConfigurationKey<Boolean>("skipInline")
    }
}
