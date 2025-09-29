package io.github.linreal.logging.gradle

open class LoggingExtension {
    /** Master on/off switch. */
    var enabled: Boolean = true
    /** Do not transform inline functions to avoid inlining-related surprises. */
    var skipInline: Boolean = true
    /** If true and Android plugin is applied, only apply in debug variants. */
    var onlyInDebug: Boolean = true
}

