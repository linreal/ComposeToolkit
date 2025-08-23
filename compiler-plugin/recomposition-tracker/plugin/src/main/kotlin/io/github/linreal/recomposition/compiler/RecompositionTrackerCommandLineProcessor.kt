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
        private const val OPTION_SKIP_INLINE = "skipInline"

        val ARG_ENABLED = CliOption(
            OPTION_ENABLED,
            "<true|false>",
            "Whether to enable recomposition tracker plugin",
            required = false,
            allowMultipleOccurrences = false
        )

        val ARG_SKIP_INLINE = CliOption(
            OPTION_SKIP_INLINE,
            "<true|false>",
            "Whether to skip inline functions",
            required = false,
            allowMultipleOccurrences = false
        )
    }

    override val pluginId: String = RecompositionTrackerCompilerPluginRegistrar.PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        ARG_ENABLED,
        ARG_SKIP_INLINE
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option) {
            ARG_ENABLED -> configuration.put(RecompositionTrackerCompilerPluginRegistrar.KEY_ENABLED, value.toBoolean())
            ARG_SKIP_INLINE -> configuration.put(RecompositionTrackerCompilerPluginRegistrar.KEY_SKIP_INLINE, value.toBoolean())
            else -> error("Unexpected config option ${option.optionName}")
        }
    }
}

