package io.github.linreal.retracker

/**
 * Marks a Composable function for recomposition tracking.
 *
 * When present, the compiler plugin injects a call to `RecomposeTracker`
 * at the start of the composable to log recomposition count and argument diffs.
 *
 * @param includeNested If true, also tracks all nested @Composable functions
 *                      recursively within this function. Default is false.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class TrackRecompositions(val includeNested: Boolean = false)
