package com.linreal.logging.gradle

/**
 * User-facing Gradle extension: `logging { ... }`.
 *
 * - `enabled`: master switch to turn the plugin on/off.
 * - `skipInline`: avoid transforming inline functions (safer for inlining).
 * - `onlyInDebug`: limit transformation to debug compilations in Android builds.
 */
open class LoggingExtension {
    /** Master on/off switch. */
    var enabled: Boolean = true
    /** Do not transform inline functions to avoid inlining-related surprises. */
    var skipInline: Boolean = true
    /** If true and Android plugin is applied, only apply in debug variants. */
    var onlyInDebug: Boolean = true
}
