package io.github.linreal.recomposition.compiler.utils

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Common IR helper extensions shared across toolkit compiler plugins.
 */
val IrFunction.valueParams: List<IrValueParameter>
    get() = parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }

@OptIn(UnsafeDuringIrConstructionAPI::class)
@Suppress("unused")
fun IrConstructorCall.valueArgumentsCount(): Int {
    return symbol.owner.parameters.count { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
}

@Suppress("unused")
fun log(message: String) {
    println("[RecompositionTrackerIrTransformer]: $message")
}
