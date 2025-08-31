package com.linreal.logging.runtime

actual fun logd(tag: String, msg: String): Int {
    println("D/$tag: $msg")
    return msg.length
}