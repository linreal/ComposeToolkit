package io.github.linreal.retracker

import platform.Foundation.NSLog

private fun log(tag: String, message: String) {
    NSLog("$tag: $message")
}

@PublishedApi
internal actual fun logDebug(tag: String, message: String) {
    log(tag, message)
}

@PublishedApi
internal actual fun logError(tag: String, message: String) {
    log(tag, message)
}
