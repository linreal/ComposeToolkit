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

- Logs are only visible in debug builds (consider adding build variant filtering)
- Currently logs to Android Logcat using `android.util.Log`
- Tracking is always enabled in the current version

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

