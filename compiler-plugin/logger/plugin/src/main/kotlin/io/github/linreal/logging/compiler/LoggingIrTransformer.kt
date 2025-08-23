package io.github.linreal.logging.compiler

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

class LoggingIrTransformer(
    private val pluginContext: IrPluginContext,
    private val skipInline: Boolean
) : IrElementTransformerVoid() {

    private val loggingAnnotationFqName = FqName("io.github.linreal.logging.Logging")

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logdSymbol by lazy {
        val id = CallableId(FqName("io.github.linreal.logging.runtime"), Name.identifier("logd"))
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
        if (!shouldTransformFunction(declaration)) {
            return super.visitFunction(declaration)
        }

        val className = getClassName(declaration)
        val functionName = declaration.name.asString()
        val logd = logdSymbol
        if (logd == null) {
            println("[LoggingPlugin] WARNING: runtime logd() not found; skip $className.$functionName")
            return super.visitFunction(declaration)
        }

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        fun logCall(suffix: String) = builder.irCall(logd).apply {
            putValueArgument(0, builder.irString(className))
            putValueArgument(1, builder.irString("$functionName $suffix"))
        }

        when (val body = declaration.body) {
            is IrBlockBody -> {
                body.statements.add(0, logCall("started"))
                if (declaration.returnType.isUnit()) {
                    body.statements.add(logCall("ended"))
                }
            }
            is IrExpressionBody -> {
                val expr = body.expression
                val isUnit = declaration.returnType.isUnit()
                declaration.body = builder.irBlockBody {
                    +logCall("started")
                    if (isUnit) {
                        +expr
                        +logCall("ended")
                    } else {
                        val tmp = irTemporary(expr, nameHint = "result")
                        +tmp
                        +logCall("ended")
                        +irReturn(irGet(tmp))
                    }
                }
            }
            else -> {
                // no-op
            }
        }

        return super.visitFunction(declaration)
    }

    private fun shouldTransformFunction(declaration: IrFunction): Boolean {
        if (!declaration.hasAnnotation(loggingAnnotationFqName)) return false
        if (declaration.isExternal || (skipInline && declaration.isInline) || declaration.body == null) return false
        return true
    }

    private fun getClassName(declaration: IrFunction): String {
        return when (val parent = declaration.parent) {
            is IrClass -> parent.name.asString()
            is IrFile -> "FileKt"
            else -> "Unknown"
        }
    }
}

