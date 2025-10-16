package io.github.linreal.retracker

/**
 * Excludes a Composable function from recomposition tracking.
 *
 * When a parent composable is marked with `@TrackRecompositions(includeNested = true)`,
 * all nested composable functions are automatically tracked. Use this annotation to
 * explicitly exclude specific nested composables from tracking.
 *
 * This is useful for:
 * - High-frequency composables where tracking overhead is undesirable
 * - Library composables that should not be included in application tracking
 * - Composables with sensitive data that should not be logged
 * - Performance-critical sections where minimal overhead is required
 *
 * Example:
 * ```
 * @TrackRecompositions(includeNested = true)
 * @Composable
 * fun ParentScreen() {
 *     TrackedChild()        // Will be tracked
 *     UntrackedChild()      // Will NOT be tracked
 * }
 *
 * @Composable
 * fun TrackedChild() { /* ... */ }
 *
 * @SkipRecompositionTracking
 * @Composable
 * fun UntrackedChild() { /* ... */ }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class SkipRecompositionTracking
