package io.github.linreal.recomposition.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class RecompositionTrackerIrGenerationExtension(
    private val skipInline: Boolean
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val transformer = RecompositionTrackerIrTransformer(pluginContext, skipInline)
        moduleFragment.transformChildrenVoid(transformer)
        transformer.finish()
    }
}
