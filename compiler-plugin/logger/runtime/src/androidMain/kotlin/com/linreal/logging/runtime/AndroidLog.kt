package com.linreal.logging.runtime

actual fun logd(tag: String, msg: String): Int = android.util.Log.d(tag, msg)

