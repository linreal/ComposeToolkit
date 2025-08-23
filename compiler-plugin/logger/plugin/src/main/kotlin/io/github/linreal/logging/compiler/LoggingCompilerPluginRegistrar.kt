package io.github.linreal.logging.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
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
        const val PLUGIN_ID = "io.github.linreal.plugin.logging"
        
        val KEY_ENABLED = CompilerConfigurationKey<Boolean>("enabled")
        val KEY_SKIP_INLINE = CompilerConfigurationKey<Boolean>("skipInline")
    }
}

