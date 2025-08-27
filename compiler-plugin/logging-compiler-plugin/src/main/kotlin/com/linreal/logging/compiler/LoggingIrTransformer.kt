package com.linreal.logging.compiler

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
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
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf.className
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class LoggingIrTransformer(
    private val pluginContext: IrPluginContext,
    private val skipInline: Boolean
) : IrElementTransformerVoid() {

    private val loggingAnnotationFqName = FqName("com.linreal.logging.Logging")

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logDSymbol by lazy {
        val id = CallableId(
            packageName = FqName("com.linreal.logging.runtime"),
            className = FqName("AndroidLog"),
            callableName = Name.identifier("d")
        )
        pluginContext.referenceFunctions(id).firstOrNull { symbol ->
            val fn = symbol.owner
            val valueParams = fn.parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
            
            valueParams.size == 2 &&
                    valueParams[0].type.isString() &&
                    valueParams[1].type.isString()
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logd2 by lazy {
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
        println("[LoggingPlugin] start this shit")

        if (!shouldTransformFunction(declaration)) {
            return super.visitFunction(declaration)
        }
        println("[LoggingPlugin] VISIT STARTED")

        val className = getClassName(declaration)
        val functionName = declaration.name.asString()
        val logD = logd2
        if (logD == null) {
            // If AndroidLog isn't on classpath, fail gracefully.
            println("[LoggingPlugin] WARNING: AndroidLog.d not found; skip $className.$functionName")
            return super.visitFunction(declaration)
        }
        
        println("[LoggingPlugin] Found AndroidLog.d symbol: ${logD.owner}")
        println("[LoggingPlugin] Parameters: ${logD.owner.valueParameters.map { "${it.name}: ${it.type}" }}")

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        fun logCall(suffix: String) = builder.irCall(logD).apply {
            putValueArgument(0, builder.irString(className))
            putValueArgument(1, builder.irString("$functionName $suffix"))
        }

        when (val body = declaration.body) {
            is IrBlockBody -> {
                println("[LoggingPlugin] adding to IrBlockBody")
                // 1) Insert "started" at the top
                body.statements.add(0, logCall("started"))

                // 3) If function returns Unit and can fall through, append "ended" at tail
                if (declaration.returnType.isUnit()) {
                    body.statements.add(logCall("ended"))
                }
            }

            is IrExpressionBody -> {
                // Convert expression body to block body so we can inject logs
                val expr = body.expression
                val isUnit = declaration.returnType.isUnit()
                declaration.body = builder.irBlockBody {
                    +logCall("started")
                    if (isUnit) {
                        +expr
                        +logCall("ended")
                        // implicit return Unit
                    } else {
                        val tmp = irTemporary(expr, nameHint = "result")
                        +tmp
                        +logCall("ended")
                        +irReturn(irGet(tmp))
                    }
                }
            }

            else -> {
                println("[LoggingPlugin] whatb the fuck ${declaration.body}")

                // Unexpected body type; proceed without transform
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
            is IrFile -> "FileKt" // simple top-level fallback
            else -> "Unknown"
        }
    }
}
