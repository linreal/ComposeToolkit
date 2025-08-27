package com.linreal.logging.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class LoggingIrGenerationExtension(
    private val skipInline: Boolean
) : IrGenerationExtension {
    /**
     * Called by the compiler for each module. We traverse the IR tree and
     * transform functions annotated with `@Logging` using [LoggingIrTransformer].
     */
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        println("[LoggingIrGenerationExtension] generate called for module: ${moduleFragment.name}")
        moduleFragment.transformChildrenVoid(
            LoggingIrTransformer(pluginContext, skipInline)
        )
    }
}
