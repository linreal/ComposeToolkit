package com.linreal.logging.runtime

fun logd(tag: String, msg: String): Int = android.util.Log.d(tag, msg)

