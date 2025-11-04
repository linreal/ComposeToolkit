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
    val previousArgs = remember { mutableMapOf<String, Any?>() }
    val isInitialComposition = remember { Ref(true) }

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
            logDebug(LOGGER_TAG, "$name exits composition, params: ($previousArgs)")
        }
    }

    SideEffect {
        isInitialComposition.value = false
    }

    val changesLog = remember { StringBuilder() }
    changesLog.clear()

    var hasSignificantChanges = false
    var hasAnyChanges = false

    val currentUnwrappedArgs = mutableMapOf<String, Any?>()
    for ((argumentName, currentValue) in arguments) {
        currentUnwrappedArgs[argumentName] = if (currentValue is State<*>) {
            currentValue.value
        } else {
            currentValue
        }
    }

    if (!isInitialComposition.value) {
        for ((argumentName, currentUnwrappedValue) in currentUnwrappedArgs) {
            if (previousArgs.containsKey(argumentName) && currentUnwrappedValue != previousArgs[argumentName]) {
                hasAnyChanges = true
                val isSkipped = skippedArgumentNames.contains(argumentName)

                if (!isSkipped) {
                    hasSignificantChanges = true
                    val previousUnwrappedValue = previousArgs[argumentName]
                    changesLog.apply {
                        append("\n••• $argumentName: ")
                        // Add a (State) hint if the *original* argument was a State
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


    previousArgs.clear()
    previousArgs.putAll(currentUnwrappedArgs)

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