package io.github.linreal.retracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.SideEffect
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
    val previousArgs = remember { arguments.toMutableMap() }
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

    for ((argumentName, currentValue) in arguments) {
        if (previousArgs.containsKey(argumentName) && currentValue != previousArgs[argumentName]) {
            hasAnyChanges = true
            val isSkipped = skippedArgumentNames.contains(argumentName)

            if (!isSkipped) {
                hasSignificantChanges = true
                val previousValue = previousArgs[argumentName]
                changesLog.apply {
                    append("\n••• $argumentName: ")
                    append("${previousValue.safeToString()} (${previousValue.safeHashCode()}) → ")
                    append("${currentValue.safeToString()} (${currentValue.safeHashCode()})")
                }
            }
        }
    }

    if (hasAnyChanges) {
        previousArgs.clear()
        previousArgs.putAll(arguments)
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