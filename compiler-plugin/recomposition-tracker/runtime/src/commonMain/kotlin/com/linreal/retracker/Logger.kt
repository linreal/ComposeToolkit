package com.linreal.retracker

import com.linreal.logging.runtime.logd

// Delegate logging to the multiplatform logger runtime
fun logInfo(tag: String, msg: String) {
    logd(tag, msg)
}

fun logDebug(tag: String, msg: String) {
    logd(tag, msg)
}
