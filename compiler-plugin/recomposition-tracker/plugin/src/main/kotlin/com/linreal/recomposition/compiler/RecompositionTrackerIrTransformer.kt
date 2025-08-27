package com.linreal.recomposition.compiler

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Stub transformer for a future Recomposition Tracker plugin.
 *
 * This class is intentionally empty for now and serves as a placeholder
 * for future IR transformations that will track Jetpack Compose recompositions.
 */
class RecompositionTrackerIrTransformer : IrElementTransformerVoid() {
    override fun visitFunction(declaration: IrFunction): org.jetbrains.kotlin.ir.IrStatement {
        // No-op for now; will be extended in future tasks.
        return super.visitFunction(declaration)
    }
}

