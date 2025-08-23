package io.github.linreal.logging

/**
 * Marker annotation used by the logging compiler plugin.
 *
 * When a function is annotated with `@Logging`, the compiler plugin
 * injects two calls around the function body:
 * - at the beginning: `Log.d(className, "functionName started")`
 * - at the end:      `Log.d(className, "functionName ended")`
 *
 * Target is limited to functions only; retention is BINARY so the
 * annotation is present in class files (so the compiler can see it)
 * but is not necessarily available via reflection at runtime.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class Logging
