package io.github.linreal.recomposition.compiler

import io.github.linreal.recomposition.compiler.utils.valueParams
import java.util.ArrayDeque
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Instruments composable functions with calls to `RecomposeTracker`.
 *
 * The transformer operates in two phases:
 * 1. **Analysis:** During the normal IR traversal we collect a lightweight [FunctionInfo] snapshot
 *    for every function (annotation state, whether it is composable, and which other composables it
 *    references).
 * 2. **Instrumentation:** After the traversal completes, [`finish`] resolves the full set of
 *    functions that must be tracked (explicit annotations plus any reachable composables when
 *    `includeNested = true`) and injects the tracker call into each body.
 *
 * Deferring instrumentation until the end ensures we have visibility into transitive calls before
 * deciding which functions need the tracking hook.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
class RecompositionTrackerIrTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {

    private val composableFqName = FqName("androidx.compose.runtime.Composable")
    private val readOnlyComposableFqName = FqName("androidx.compose.runtime.ReadOnlyComposable")
    private val trackRecompositionsAnnotationFqName =
        FqName("io.github.linreal.retracker.TrackRecompositions")

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val recomposeTrackerSymbol by lazy {
        val id =
            CallableId(FqName("io.github.linreal.retracker"), Name.identifier("RecompositionTracker"))
        pluginContext.referenceFunctions(id).firstOrNull { it.owner.valueParams.size == 2 }
    }

    /** Metadata index built during the analysis phase */
    private val functionInfoMap = mutableMapOf<IrFunctionSymbol, FunctionInfo>()
    /** Keeps track of functions that already received an injected tracker call */
    private val instrumentedFunctions = mutableSetOf<IrFunctionSymbol>()
    /** Ensures the instrumentation pass executes at most once */
    private var instrumentationApplied = false

    /**
     * Snapshot of the properties we care about for a single function. This is lightweight enough to
     * gather eagerly for every declaration and allows us to resolve nested tracking decisions later
     * without revisiting IR
     */
    private data class FunctionInfo(
        val function: IrFunction,
        val isComposable: Boolean,
        val hasTrackAnnotation: Boolean,
        val includeNested: Boolean,
        val calledComposableSymbols: Set<IrFunctionSymbol>
    )

    override fun visitFunction(declaration: IrFunction): IrStatement {
        declaration.transformChildrenVoid(this)

        if (!declaration.hasAnnotation(composableFqName) || declaration.hasAnnotation(
                readOnlyComposableFqName
            )
        ) return declaration

        val hasAnnotation = declaration.hasAnnotation(trackRecompositionsAnnotationFqName)
        val includeNested = hasAnnotation && getIncludeNestedValue(declaration)
        val info = FunctionInfo(
            function = declaration,
            isComposable = true,
            hasTrackAnnotation = hasAnnotation,
            includeNested = includeNested,
            calledComposableSymbols = collectComposableCalls(declaration)
        )
        functionInfoMap[declaration.symbol] = info

        return declaration
    }

    /**
     * Returns composable functions referenced from the given function body, including lambdas and
     * function references. The result feeds the breadth-first search used for nested tracking.
     */
    private fun collectComposableCalls(function: IrFunction): Set<IrFunctionSymbol> {
        val body = function.body ?: return emptySet()
        val collector = ComposableCallCollector()
        body.accept(collector, null)
        return collector.calledSymbols
    }

    /**
     * Visitor that captures every composable call site reachable from a function body.
     */
    private inner class ComposableCallCollector : IrVisitorVoid() {
        val calledSymbols = linkedSetOf<IrFunctionSymbol>()

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitCall(expression: IrCall) {
            val symbol = expression.symbol
            val owner = symbol.owner
            if (owner.hasAnnotation(composableFqName)) {
                calledSymbols += symbol
            }
            expression.acceptChildrenVoid(this)
        }

        override fun visitFunctionExpression(expression: IrFunctionExpression) {
            val function = expression.function
            if (function.hasAnnotation(composableFqName)) {
                calledSymbols += function.symbol
            }
            super.visitFunctionExpression(expression)
        }

        override fun visitFunctionReference(expression: IrFunctionReference) {
            val symbol = expression.symbol
            if (symbol.owner.hasAnnotation(composableFqName)) {
                calledSymbols += symbol
            }
            super.visitFunctionReference(expression)
        }
    }

    /**
     * Executes the instrumentation pass once the entire module has been scanned
     */
    private fun applyInstrumentation() {
        if (instrumentationApplied) return
        instrumentationApplied = true

        val trackedFunctions = computeTrackedFunctions()
        trackedFunctions.forEach { instrumentFunction(it) }
    }

    /**
     * Entry point invoked by the generation extension after traversal ends
     */
    fun finish() {
        applyInstrumentation()
    }

    /**
     * Builds the set of functions that need instrumentation by combining explicitly annotated
     * composables with the transitive closure of composable calls when nested tracking is enabled
     */
    private fun computeTrackedFunctions(): Set<IrFunction> {
        val result = linkedSetOf<IrFunction>()
        val queue = ArrayDeque<IrFunctionSymbol>()

        functionInfoMap.values.forEach { info ->
            if (!info.isComposable) return@forEach
            if (info.hasTrackAnnotation) {
                result += info.function
                if (info.includeNested) {
                    queue += info.function.symbol
                }
            }
        }

        val visited = mutableSetOf<IrFunctionSymbol>()
        while (queue.isNotEmpty()) {
            val symbol = queue.removeFirst()
            if (!visited.add(symbol)) continue
            val info = functionInfoMap[symbol] ?: continue
            info.calledComposableSymbols.forEach { calleeSymbol ->
                val calleeInfo = functionInfoMap[calleeSymbol] ?: return@forEach
                if (!calleeInfo.isComposable) return@forEach
                if (result.add(calleeInfo.function)) {
                    queue += calleeSymbol
                }
            }
        }

        return result
    }

    /**
     * Injects the `RecomposeTracker` invocation at the top of the provided composable's body
     *
     * We keep track of already-instrumented symbols to prevent duplicate inserts when a symbol is
     * reachable along multiple paths
     */
    private fun instrumentFunction(function: IrFunction) {
        val symbol = function.symbol
        if (!function.hasAnnotation(composableFqName)) return
        if (!instrumentedFunctions.add(symbol)) return
        val trackerSymbol = recomposeTrackerSymbol ?: return

        val body = function.body ?: return

        val builder = DeclarationIrBuilder(pluginContext, symbol)
        val trackerOwner = trackerSymbol.owner
        val trackerCall = builder.irCall(trackerSymbol).apply {
            val name = function.fqNameWhenAvailable?.asString() ?: function.name.asString()
            val nameParam = trackerOwner.valueParams[0]
            val argsParam = trackerOwner.valueParams[1]
            arguments[nameParam.indexInParameters] = builder.irString(name)
            arguments[argsParam.indexInParameters] = buildArgumentsMap(builder, function)
        }

        when (body) {
            is IrBlockBody -> {
                body.statements.add(0, trackerCall)
            }

            is IrExpressionBody -> {
                val expr = body.expression
                function.body = builder.irBlockBody {
                    +trackerCall
                    +expr
                }
            }

            else -> {}
        }
    }

    /**
     * Extracts the `includeNested` flag from the `@TrackRecompositions` annotation. The annotation
     * currently exposes a single boolean argument, so we read the first entry directly.
     */
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun getIncludeNestedValue(declaration: IrFunction): Boolean {
        val annotation =
            declaration.getAnnotation(trackRecompositionsAnnotationFqName) ?: return false
        val constructor = annotation.symbol.owner
        val includeNestedParam = constructor.valueParams.firstOrNull() ?: return false

        if (includeNestedParam.indexInParameters < annotation.arguments.size) {
            val arg = annotation.arguments[includeNestedParam.indexInParameters]
            // Extract boolean value from IrConst
            if (arg is IrConst) {
                return arg.value as? Boolean ?: false
            }
        }
        return false
    }

    /**
     * Builds a mutable map containing the composable's parameter snapshot that the tracker uses to
     * diff argument changes between recompositions
     */
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
            val tmp = irTemporary(
                irCall(mutableMapOf).apply {
                    typeArguments[0] = stringType
                    typeArguments[1] = anyNType
                },
                nameHint = "args"
            )

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
