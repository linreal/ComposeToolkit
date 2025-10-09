package io.github.linreal.retracker

private fun log(tag: String, message: String) {
    println("$tag: $message")
}

@PublishedApi
internal actual fun logDebug(tag: String, message: String) {
    log(tag, message)
}

@PublishedApi
internal actual fun logError(tag: String, message: String) {
    log(tag, message)
}
