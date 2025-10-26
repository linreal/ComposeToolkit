# ComposeToolkit Project Structure

## Overview
Kotlin compiler plugin toolkit for Jetpack Compose performance analysis and debugging. Contains multiple compiler plugins with runtime libraries and Gradle integration.

## Root Configuration
```
|-- settings.gradle.kts          # Project module definitions
|-- build.gradle.kts             # Root build configuration
|-- build-logic/                 # Included build with shared convention plugins
|-- gradle/libs.versions.toml    # Version catalog
|-- gradle.properties            # Gradle properties
|-- local.properties             # Local environment config
`-- gradlew[.bat]                # Gradle wrapper scripts
```

## Main Modules

### 1. Android Demo App (`/app-local`)
```
app-local/
|-- build.gradle.kts            # Android app build config (uses direct project dependencies)
|-- src/main/
|   |-- AndroidManifest.xml
|   |-- java/io/github/linreal/composetoolkit/
|   |   |-- MainActivity.kt     # Main activity with recomposition tracking demos
|   |   `-- ui/theme/          # Compose theme setup
|   `-- res/                   # Android resources
```

### 2. Sample App (`/sample`)
```
sample/
|-- build.gradle.kts            # Android app build config (uses published plugin via alias)
|-- src/main/
|   |-- AndroidManifest.xml
|   |-- java/io/github/linreal/composetoolkit/sample/
|   |   |-- MainActivity.kt     # Main activity (same as app module)
|   |   `-- ui/theme/          # Compose theme setup
|   `-- res/                   # Android resources
```
**Purpose**: Demonstrates plugin usage via Gradle version catalog, simulating real-world integration

### 3. Compiler Plugins (`/compiler-plugin`)
Two main compiler plugins, each with plugin + runtime structure:



#### Recomposition Tracker Plugin (`/compiler-plugin/recomposition-tracker`)
```
recomposition-tracker/
|-- plugin/                    # Kotlin compiler plugin
|   |-- build.gradle.kts
|   `-- src/main/kotlin/io/github/linreal/recomposition/compiler/
|       |-- RecompositionTrackerCommandLineProcessor.kt
|       |-- RecompositionTrackerCompilerPluginRegistrar.kt
|       |-- RecompositionTrackerIrGenerationExtension.kt
|       `-- RecompositionTrackerIrTransformer.kt
`-- runtime/                   # Multiplatform runtime library
    |-- build.gradle.kts
    `-- src/
        |-- commonMain/kotlin/io/github/linreal/retracker/
        |   |-- Logger.kt
        |   |-- RecompositionTracker.kt
        |   `-- TrackRecompositions.kt
        |-- androidMain/kotlin/io/github/linreal/retracker/
        |   `-- Logger.android.kt
        |-- iosMain/kotlin/io/github/linreal/retracker/
        |   `-- Logger.ios.kt
        `-- wasmJsMain/kotlin/io/github/linreal/retracker/
            `-- Logger.wasm.kt
```

### 4. Gradle Plugins (`/gradle-plugin`)
```
gradle-plugin/
|-- toolkit/                   # Main user-facing plugin
|   |-- build.gradle.kts
|   `-- src/main/kotlin/io/github/linreal/toolkit/
|       `-- ComposeToolkitGradlePlugin.kt  # Applies all sub-plugins
`-- sub-plugin/               # Individual feature plugins
    `-- recomposition-tracker/
        |-- build.gradle.kts
        `-- src/main/kotlin/io/github/linreal/recomposition/gradle/
            `-- RecompositionTrackerGradlePlugin.kt
```

### 5. Build Logic (`/build-logic`)
```
build-logic/
|-- build.gradle.kts           # Convention plugin project (kotlin-dsl)
|-- settings.gradle.kts        # Included build configuration
`-- src/main/kotlin/io/github/linreal/gradle/
    `-- PublishingConventionsPlugin.kt  # Shared publishing defaults for Maven Central
```
**Purpose**: Hosts reusable Gradle conventions (currently Maven Central publishing block) that can be applied across modules.

## Module Dependencies
- **App**: Uses runtime libraries from both plugins
- **Compiler Plugins**: Independent modules that transform Kotlin IR
- **Runtime Libraries**: Provide APIs consumed by generated code
- **Gradle Plugins**: Apply compiler plugins and configure projects
- **Annotations**: Shared annotation definitions

## Key Technologies
- **Kotlin**: 2.2.10 with K2 compiler support
- **Android Gradle Plugin**: 8.12.1
- **Jetpack Compose**: 2025.08.00 BOM
- **Compiler Plugin API**: Kotlin compiler embeddable

## Plugin Architecture
Each plugin follows the standard pattern:
1. **Compiler Plugin**: IR transformation during compilation
2. **Runtime Library**: APIs and utilities for generated code
3. **Gradle Plugin**: Build system integration and configuration
4. **Annotations**: Compile-time markers and configuration
