package io.github.linreal.retracker

import android.util.Log

@PublishedApi
internal actual fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

@PublishedApi
internal actual fun logError(tag: String, message: String) {
    Log.e(tag, message)
}
