package io.github.linreal.retracker

@PublishedApi
internal expect fun logDebug(tag: String, message: String)

@PublishedApi
internal expect fun logError(tag: String, message: String)
