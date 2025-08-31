package com.linreal.retracker

/**
 * Marks a Composable function for recomposition tracking.
 *
 * When present, the compiler plugin injects a call to `RecomposeTracker`
 * at the start of the composable to log recomposition count and argument diffs.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class TrackRecompositions

