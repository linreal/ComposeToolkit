package io.github.linreal.retracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

@PublishedApi
internal const val LOGGER_TAG: String = "RecompositionTracker"

@NoLiveLiterals
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun RecompositionTracker(
    name: String,
    arguments: Map<String, Any?>,
    skippedArgumentNames: Set<String> = emptySet()
) {
    if (!RecompositionTrackingSettings.isEnabled) {
        return
    }
    val loggedRecompositionCount = remember { Ref(0) }
    val isInitialComposition = remember { Ref(true) }

    // Remember two maps to swap between. This avoids allocating a new
    // map on every recomposition, which is a major source of GC pressure.
    val maps = remember {
        arrayOf(
            mutableMapOf<String, Any?>(),
            mutableMapOf<String, Any?>()
        )
    }
    val currentMapIndex = remember { Ref(0) }

    LaunchedEffect(Unit) {
        val renderedArgs = if (arguments.isNotEmpty()) {
            arguments.entries.joinToString(separator = ", ") { (key, value) ->
                "$key: ${value.safeToString()}"
            }
        } else {
            "no arguments"
        }
        logDebug(LOGGER_TAG, "$name enters composition, params: ($renderedArgs) ")
    }

    DisposableEffect(Unit) {
        onDispose {
            val lastArgs = maps[currentMapIndex.value]
            logDebug(LOGGER_TAG, "$name exits composition, params: ($lastArgs)")
        }
    }

    SideEffect {
        isInitialComposition.value = false
    }
    val changesLog = remember { StringBuilder() }
    changesLog.clear()

    var hasSignificantChanges = false
    var hasAnyChanges = false

    val previousUnwrappedArgs = maps[currentMapIndex.value]
    currentMapIndex.value = (currentMapIndex.value + 1) % 2
    val currentUnwrappedArgs = maps[currentMapIndex.value]
    currentUnwrappedArgs.clear()

    for ((argumentName, currentValue) in arguments) {
        currentUnwrappedArgs[argumentName] = if (currentValue is State<*>) {
            currentValue.value
        } else {
            currentValue
        }
    }

    if (!isInitialComposition.value) {
        for ((argumentName, currentUnwrappedValue) in currentUnwrappedArgs) {
            if (previousUnwrappedArgs.containsKey(argumentName) && currentUnwrappedValue != previousUnwrappedArgs[argumentName]) {
                hasAnyChanges = true
                val isSkipped = skippedArgumentNames.contains(argumentName)

                if (!isSkipped) {
                    hasSignificantChanges = true
                    val previousUnwrappedValue = previousUnwrappedArgs[argumentName]
                    changesLog.apply {
                        append("\n••• $argumentName: ")
                        if (arguments[argumentName] is State<*>) {
                            append("(State) ")
                        }
                        append("${previousUnwrappedValue.safeToString()} (${previousUnwrappedValue.safeHashCode()}) → ")
                        append("${currentUnwrappedValue.safeToString()} (${currentUnwrappedValue.safeHashCode()})")
                    }
                }
            }
        }
    }

    if (!isInitialComposition.value) {
        val shouldLog = hasSignificantChanges || !hasAnyChanges

        if (shouldLog) {
            loggedRecompositionCount.value++

            if (hasSignificantChanges) {
                logDebug(LOGGER_TAG, "$name recomposed ${loggedRecompositionCount.value} times")
                logDebug(LOGGER_TAG, "Changes:$changesLog\n")
            } else {
                logDebug(
                    LOGGER_TAG,
                    "$name recomposed ${loggedRecompositionCount.value} times due to internal state change\n"
                )
            }
        }
    }
}

@PublishedApi
internal class Ref<T>(var value: T)

@PublishedApi
internal fun Any?.safeHashCode(): Int = this?.hashCode() ?: 0

@PublishedApi
internal fun Any?.safeToString(): String = this?.toString() ?: "null"