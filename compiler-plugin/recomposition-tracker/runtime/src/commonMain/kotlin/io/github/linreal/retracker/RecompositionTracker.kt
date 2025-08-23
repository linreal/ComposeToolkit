package io.github.linreal.retracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

@PublishedApi
internal const val LOGGER_TAG: String = "RecompositionTracker"

@NoLiveLiterals
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun RecompositionTracker(
    name: String,
    arguments: Map<String, Any?>
) {
    val refCount = remember { Ref(0) }

    val previousArgs = remember { mutableStateOf(arguments.toMap()) }

    LaunchedEffect(Unit) {
        val renderedArgs = if (arguments.isNotEmpty()) {
            arguments.entries.joinToString(separator = ", ") { (key, value) ->
                "$key: ${value.safeToString()}"
            }
        } else {
            "no arguments"
        }
        logDebug(LOGGER_TAG, "$name ($renderedArgs) enters composition")
    }
    DisposableEffect(Unit) {
        onDispose {
            logDebug(LOGGER_TAG, "$name exits composition")
        }
    }
    SideEffect { refCount.count++ }

    val changesLog = remember { StringBuilder() }
    changesLog.clear()

    for ((argumentName, currentValue) in arguments) {
        val previousValue = previousArgs.value[argumentName]

        if (currentValue != previousValue) {
            changesLog.apply {
                append("\n••• $argumentName: ")
                append("${previousValue.safeToString()} (${previousValue.safeHashCode()}) → ")
                append("${currentValue.safeToString()} (${currentValue.safeHashCode()})")
            }
        }
    }

    if (changesLog.isNotEmpty()) {
        previousArgs.value = arguments.toMap()
    }

    logDebug(LOGGER_TAG, "$name recomposed ${refCount.count} times")
    if (changesLog.isNotEmpty()) {
        logDebug(LOGGER_TAG, "Changes:$changesLog\n")
    }
}

@PublishedApi
internal class Ref(var count: Int = 0)

@PublishedApi
internal fun Any?.safeHashCode(): Int = this?.hashCode() ?: 0

@PublishedApi
internal fun Any?.safeToString(): String = this?.toString() ?: "null"