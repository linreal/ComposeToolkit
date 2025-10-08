package io.github.linreal.retracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

@PublishedApi
internal const val LOGGER_TAG: String = "RecomposeLogger"

@NoLiveLiterals
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun RecomposeTracker(
    name: String,
    arguments: Map<String, Any?>
) {
    LaunchedEffect(Unit) {
        val renderedArgs = arguments.entries.joinToString { "${it.key} : ${it.value}" }
        logError(LOGGER_TAG, "Begin tracking, $name, $renderedArgs")
    }

    val ref = remember { Ref(0) }
    SideEffect { ref.count++ }

    val recomposeLog = StringBuilder()

    for ((argumentName, argumentValue) in arguments) {
        val dataDiff = remember { DataDiffHolder(argumentValue) }
        dataDiff.setNewValue(argumentValue)

        if (dataDiff.isChanged()) {
            val previous = dataDiff.previous
            val current = dataDiff.current
            recomposeLog.append("\n\t $argumentName changed: prev=[value=$previous, hashcode = ${previous.hashCode()}], current=[value=$current, hashcode = ${current.hashCode()}]")
        }
    }

    val isEnabled = true //RecomposeLoggerConfig.isEnabled
    if (isEnabled) {
        logDebug(LOGGER_TAG, "$name recomposed ${ref.count} times.")
        if (recomposeLog.isNotEmpty()) {
            logDebug(LOGGER_TAG, "Changes:${recomposeLog}\n")
        }
    }
}

class DataDiffHolder(current: Any?) {
    var current: Any? = current
        private set

    var previous: Any? = null
        private set

    fun isChanged() = current != previous

    fun setNewValue(newCurrent: Any?) {
        previous = current
        current = newCurrent
    }
}

data class Ref(var count: Int = 0)
