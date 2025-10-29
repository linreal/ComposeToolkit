package io.github.linreal.recomposition.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class RecompositionTrackerCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    private fun CompilerConfiguration.msg(): MessageCollector =
        get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val mc = configuration.msg()
        val enabled = configuration.get(KEY_ENABLED, false)
        mc.report(CompilerMessageSeverity.WARNING, "[RecompositionTracker] enabled=$enabled")

        if (enabled) {
            IrGenerationExtension.registerExtension(
                RecompositionTrackerIrGenerationExtension()
            )
            mc.report(CompilerMessageSeverity.WARNING, "[RecompositionTracker] IR extension registered")
        }
    }

    companion object {
        const val PLUGIN_ID = "io.github.linreal.plugin.recomposition-tracker"

        val KEY_ENABLED = CompilerConfigurationKey<Boolean>("enabled")
    }
}

