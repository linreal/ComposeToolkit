package io.github.linreal.retracker

/**
 * Excludes a Composable function or function parameter from recomposition tracking.
 *
 * ## Function-level usage
 * When a parent composable is marked with `@TrackRecompositions(includeNested = true)`,
 * all nested composable functions are automatically tracked. Use this annotation to
 * explicitly exclude specific nested composables from tracking.
 *
 * ## Parameter-level usage
 * When applied to a function parameter, the recomposition tracker will not increment
 * the counter or log anything if the composable recomposes **only** because of changes
 * to parameters marked with this annotation.
 *
 * This is useful for:
 * - High-frequency composables where tracking overhead is undesirable
 * - Library composables that should not be included in application tracking
 * - Composables with sensitive data that should not be logged
 * - Performance-critical sections where minimal overhead is required
 * - Parameters that change frequently but are not important for debugging (e.g., modifiers, colors)
 * - Parameters used for animations or visual effects that shouldn't trigger tracking
 *
 * ## Function-level Example:
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
 *
 * ## Parameter-level Example:
 * ```
 * @TrackRecompositions
 * @Composable
 * fun AnimatedButton(
 *     text: String,
 *     onClick: () -> Unit,
 *     @SkipRecompositionTracking modifier: Modifier = Modifier,
 *     @SkipRecompositionTracking animationProgress: Float = 0f
 * ) {
 *     // If only 'modifier' or 'animationProgress' changes, no recomposition will be logged
 *     // If 'text' or 'onClick' changes, recomposition will be logged normally
 *     // Logs will show all parameters but mark skipped ones with ○○○ instead of •••
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class SkipRecompositionTracking
