package io.github.linreal.recomposition.compiler

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.serialization.kind
import org.jetbrains.kotlin.backend.konan.KonanFqNames.function
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * IR transformer that injects a call to `RecomposeTracker` at the start of
 * Composable functions annotated with `@TrackRecompositions`.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
class RecompositionTrackerIrTransformer(
    private val pluginContext: IrPluginContext,
    private val skipInline: Boolean
) : IrElementTransformerVoid() {

    private val composableFqName = FqName("androidx.compose.runtime.Composable")
    private val trackAnnoFqName = FqName("io.github.linreal.retracker.TrackRecompositions")

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val recomposeTrackerSymbol by lazy {
        val id =
            CallableId(FqName("io.github.linreal.retracker"), Name.identifier("RecomposeTracker"))
        pluginContext.referenceFunctions(id).firstOrNull { it.owner.valueParams.size == 2 }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        declaration.transformChildrenVoid(this)
        // Only process real functions with bodies
        val body = declaration.body ?: return declaration
        if (skipInline && declaration.isInline) return declaration
        if (!declaration.hasAnnotation(trackAnnoFqName)) return declaration
        if (!declaration.hasAnnotation(composableFqName)) return declaration

        val trackerSymbol = recomposeTrackerSymbol ?: return declaration

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        val trackerOwner = trackerSymbol.owner
        val trackerCall = builder.irCall(trackerSymbol).apply {
            val name = declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString()
            val nameParam = trackerOwner.valueParams[0]
            val argsParam = trackerOwner.valueParams[1]
            arguments[nameParam.indexInParameters] = builder.irString(name)
            arguments[argsParam.indexInParameters] = buildArgumentsMap(builder, declaration)
        }

        when (body) {
            is IrBlockBody -> {
                body.statements.add(0, trackerCall)
            }

            is IrExpressionBody -> {
                val expr = body.expression
                declaration.body = builder.irBlockBody {
                    +trackerCall
                    +expr
                }
            }

            else -> { /* no-op */
            }
        }
        return declaration
    }

    private fun buildArgumentsMap(
        builder: IrBuilderWithScope,
        function: IrFunction
    ) = with(builder) {
        val stringType = pluginContext.irBuiltIns.stringType
        val anyNType = pluginContext.irBuiltIns.anyNType

        val mutableMapOf = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("mutableMapOf"))
        ).firstOrNull { it.owner.valueParams.isEmpty() }

        val emptyMapSymbol = pluginContext.referenceFunctions(
            CallableId(FqName("kotlin.collections"), Name.identifier("emptyMap"))
        ).first()

        if (mutableMapOf == null) {
            return@with irCall(emptyMapSymbol).apply {
                typeArguments[0] = stringType
                typeArguments[1] = anyNType
            }
        }

        irBlock {
            val tmp = irTemporary(irCall(mutableMapOf).apply {
                typeArguments[0] = stringType
                typeArguments[1] = anyNType
            }, nameHint = "args")

            val mutMapClass = pluginContext.irBuiltIns.mutableMapClass
            val putSymbol = (mutMapClass.owner.declarations.filterIsInstance<IrSimpleFunction>()
                .first { it.name.asString() == "put" && it.valueParams.size == 2 }).symbol
            val putOwner = putSymbol.owner
            val keyParam = putOwner.valueParams[0]
            val valueParam = putOwner.valueParams[1]
            function.valueParams.forEach { param ->
                +irCall(putSymbol).apply {
                    dispatchReceiver = irGet(tmp)
                    arguments[keyParam.indexInParameters] = irString(param.name.asString())
                    arguments[valueParam.indexInParameters] = irAs(irGet(param), anyNType)
                }
            }

            +irGet(tmp)
        }
    }
}

private val IrFunction.valueParams
    get() = this.parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
