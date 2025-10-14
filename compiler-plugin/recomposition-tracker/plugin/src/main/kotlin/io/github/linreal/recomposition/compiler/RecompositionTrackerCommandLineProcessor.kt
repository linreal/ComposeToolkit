package io.github.linreal.recomposition.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class RecompositionTrackerCommandLineProcessor : CommandLineProcessor {

    companion object {
        private const val OPTION_ENABLED = "enabled"

        val ARG_ENABLED = CliOption(
            OPTION_ENABLED,
            "<true|false>",
            "Whether to enable recomposition tracker plugin",
            required = false,
            allowMultipleOccurrences = false
        )
    }

    override val pluginId: String = RecompositionTrackerCompilerPluginRegistrar.PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        ARG_ENABLED,
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option) {
            ARG_ENABLED -> configuration.put(RecompositionTrackerCompilerPluginRegistrar.KEY_ENABLED, value.toBoolean())
            else -> error("Unexpected config option ${option.optionName}")
        }
    }
}

