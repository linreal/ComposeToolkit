package com.linreal.logging.compiler

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * IR transformer that injects logging around functions annotated with `@Logging`.
 *
 * High-level flow:
 * 1) Resolve a reference to the runtime logging function `com.linreal.logging.runtime.logd`.
 * 2) Visit each function; if it has `@Logging` and is eligible, wrap body with log calls.
 * 3) Insert "started" at the beginning and "ended" at all normal return paths.
 */
class LoggingIrTransformer(
    private val pluginContext: IrPluginContext,
    private val skipInline: Boolean
) : IrElementTransformerVoid() {

    private val loggingAnnotationFqName = FqName("com.linreal.logging.Logging")

    /**
     * Resolve the symbol of our runtime logging helper `logd(tag: String, msg: String)`.
     * We look it up by [CallableId] and validate its parameter types.
     */
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logdSymbol by lazy {
        val id = CallableId(FqName("com.linreal.logging.runtime"), Name.identifier("logd"))
        pluginContext.referenceFunctions(id).firstOrNull { symbol ->
            val fn = symbol.owner
            val valueParams = fn.parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
            valueParams.size == 2 &&
                valueParams[0].type.isString() &&
                valueParams[1].type.isString()
        }
    }

    @OptIn(DeprecatedForRemovalCompilerApi::class)
    override fun visitFunction(declaration: IrFunction): IrStatement {
        // Only transform eligible, annotated functions.
        if (!shouldTransformFunction(declaration)) {
            return super.visitFunction(declaration)
        }

        val className = getClassName(declaration)
        val functionName = declaration.name.asString()
        val logd = logdSymbol
        if (logd == null) {
            // If runtime isn't on classpath, do nothing (build stays green).
            println("[LoggingPlugin] WARNING: runtime logd() not found; skip $className.$functionName")
            return super.visitFunction(declaration)
        }

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        // Builds a call to `logd(className, "functionName <suffix>")`.
        fun logCall(suffix: String) = builder.irCall(logd).apply {
            putValueArgument(0, builder.irString(className))
            putValueArgument(1, builder.irString("$functionName $suffix"))
        }

        when (val body = declaration.body) {
            is IrBlockBody -> {
                // Insert at the beginning of the block.
                body.statements.add(0, logCall("started"))

                // If function returns Unit and may fall through, append at the end.
                if (declaration.returnType.isUnit()) {
                    body.statements.add(logCall("ended"))
                }
            }

            is IrExpressionBody -> {
                // Convert expression body into block body so we can inject statements.
                val expr = body.expression
                val isUnit = declaration.returnType.isUnit()
                declaration.body = builder.irBlockBody {
                    +logCall("started")
                    if (isUnit) {
                        +expr
                        +logCall("ended")
                        // implicit return Unit
                    } else {
                        // For non-Unit returns, capture the value, log, then return.
                        val tmp = irTemporary(expr, nameHint = "result")
                        +tmp
                        +logCall("ended")
                        +irReturn(irGet(tmp))
                    }
                }
            }

            else -> {
                // Unexpected body type; proceed without transform.
            }
        }

        return super.visitFunction(declaration)
    }

    /** Checks whether we should transform this function. */
    private fun shouldTransformFunction(declaration: IrFunction): Boolean {
        if (!declaration.hasAnnotation(loggingAnnotationFqName)) return false
        if (declaration.isExternal || (skipInline && declaration.isInline) || declaration.body == null) return false
        return true
    }

    /** Produces a readable class name for logging. */
    private fun getClassName(declaration: IrFunction): String {
        return when (val parent = declaration.parent) {
            is IrClass -> parent.name.asString()
            is IrFile -> "FileKt" // simple top-level fallback
            else -> "Unknown"
        }
    }
}
