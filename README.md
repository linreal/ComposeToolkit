# Compose Recomposition Tracker

A Kotlin compiler plugin that automatically tracks and logs recompositions in Jetpack Compose functions, helping you identify performance bottlenecks and unnecessary recompositions in your Android applications.

## Features

- **Automatic Recomposition Tracking**: Inject tracking code into `@Composable` functions at compile time
- **Detailed Logging**: Monitor recomposition counts and identify which parameter changes trigger recompositions
- **Zero Runtime Overhead When Disabled**: The plugin operates at compile time with minimal impact
- **Easy Integration**: Simple Gradle plugin setup with annotation-based configuration
- **Argument Diff Tracking**: See exactly which parameters changed and caused a recomposition

## How It Works

The plugin uses Kotlin compiler IR transformation to inject tracking code into your Composable functions. When you annotate a `@Composable` function with `@TrackRecompositions`, the plugin automatically:

1. Counts the number of times the function recomposes
2. Tracks parameter changes between recompositions
3. Logs detailed information about what changed and triggered the recomposition

## Installation

### Step 1: Add the Gradle Plugin

In your module's `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.linreal.recomposition-tracker") version "1.0.0"
}
```


## Configuration

The plugin can be configured in your module's `build.gradle.kts`:

```kotlin
recompositionTracker {
    enabled = true        // Enable or disable the plugin (default: true)
    onlyInDebug = true    // Only apply tracking in debug builds (default: true)
}
```

### Configuration Options

- **`enabled`**: Master switch to enable or disable the recomposition tracker plugin entirely.
  - `true` (default): Plugin is active and will track recompositions
  - `false`: Plugin is completely disabled, no tracking code is injected

- **`onlyInDebug`**: Controls whether the plugin should only apply to debug build variants.
  - `true` (default): Only tracks recompositions in debug builds, no overhead in release builds
  - `false`: Tracks recompositions in all build variants (debug and release)

### Configuration Examples

**Default behavior** (enabled in debug builds only):
```kotlin
// No configuration needed - uses defaults
plugins {
    id("io.github.linreal.recomposition-tracker") version "1.0.0"
}
```

**Completely disable the plugin**:
```kotlin
recompositionTracker {
    enabled = false
}
```

**Enable in all build variants** (including release):
```kotlin
recompositionTracker {
    enabled = true
    onlyInDebug = false  // Also track in release builds
}
```

**Conditional configuration**:
```kotlin
recompositionTracker {
    enabled = project.hasProperty("trackRecompositions")
    onlyInDebug = true
}
// Then run: ./gradlew assembleDebug -PtrackRecompositions
```

## Usage

Simply annotate any `@Composable` function you want to track:

```kotlin
import androidx.compose.runtime.Composable
import io.github.linreal.retracker.TrackRecompositions

@TrackRecompositions
@Composable
fun MyComposable(count: Int, name: String) {
    Text("Count: $count, Name: $name")
}
```



## Requirements

- Kotlin 2.2.10 or higher
- Android Gradle Plugin 8.12.1 or higher
- Jetpack Compose 2025.08.00 or higher
- JDK 17

## Use Cases

- **Performance Debugging**: Identify which composables are recomposing too frequently
- **Optimization**: Understand what parameter changes trigger recompositions
- **Learning Tool**: Understand how Compose's recomposition mechanism works
- **Development**: Quick debugging during feature development

## Limitations

- Currently logs to Android Logcat using `android.util.Log`
- Tracking requires annotating functions with `@TrackRecompositions`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

