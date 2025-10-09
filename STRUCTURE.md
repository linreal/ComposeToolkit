# ComposeToolkit Project Structure

## Overview
Kotlin compiler plugin toolkit for Jetpack Compose performance analysis and debugging. Contains multiple compiler plugins with runtime libraries and Gradle integration.

## Root Configuration
```
|-- settings.gradle.kts          # Project module definitions
|-- build.gradle.kts             # Root build configuration
|-- gradle/libs.versions.toml    # Version catalog
|-- gradle.properties           # Gradle properties
|-- local.properties           # Local environment config
`-- gradlew[.bat]              # Gradle wrapper scripts
```

## Main Modules

### 1. Android Demo App (`/app`)
```
app/
|-- build.gradle.kts            # Android app build config
|-- src/main/
|   |-- AndroidManifest.xml
|   |-- java/io/github/linreal/composetoolkit/
|   |   |-- MainActivity.kt     # Main activity
|   |   `-- ui/theme/          # Compose theme setup
|   `-- res/                   # Android resources
|-- src/test/                  # Unit tests
`-- src/androidTest/           # Instrumentation tests
```

### 2. Compiler Plugins (`/compiler-plugin`)
Two main compiler plugins, each with plugin + runtime structure:

#### Logger Plugin (`/compiler-plugin/logger`)
```
logger/
|-- plugin/                    # Kotlin compiler plugin
|   |-- build.gradle.kts
|   `-- src/main/kotlin/io/github/linreal/logging/compiler/
|       |-- LoggingCommandLineProcessor.kt
|       |-- LoggingCompilerPluginRegistrar.kt
|       |-- LoggingIrGenerationExtension.kt
|       `-- LoggingIrTransformer.kt
`-- runtime/                   # Runtime library
    |-- build.gradle.kts
    `-- src/main/kotlin/io/github/linreal/logging/runtime/
        `-- AndroidLog.kt
```

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

### 3. Gradle Plugins (`/gradle-plugin`)
```
gradle-plugin/
|-- toolkit/                   # Main user-facing plugin
|   |-- build.gradle.kts
|   `-- src/main/kotlin/io/github/linreal/toolkit/
|       `-- ComposeToolkitGradlePlugin.kt  # Applies all sub-plugins
`-- sub-plugin/               # Individual feature plugins
    |-- logger/
    |   |-- build.gradle.kts
    |   `-- src/main/kotlin/io/github/linreal/logging/gradle/
    |       |-- LoggingExtension.kt
    |       `-- LoggingGradlePlugin.kt
    `-- recomposition-tracker/
        |-- build.gradle.kts
        `-- src/main/kotlin/io/github/linreal/recomposition/gradle/
            `-- RecompositionTrackerGradlePlugin.kt
```

### 4. Annotations (`/logging-annotations`)
```
logging-annotations/
|-- build.gradle.kts
`-- src/main/kotlin/io/github/linreal/logging/
    `-- Logging.kt             # Annotation definitions
```

### 5. Task Documentation (`/cc_tasks`)
```
cc_tasks/
|-- STRUCTURE.md               # This file
|-- LoggerPluginTask.md       # Logger plugin development task
|-- RecompositionPluginTask.md # Recomposition tracker task
`-- ModuleReorganization.md   # Module restructuring task
```

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

This modular structure allows independent development and testing of each component while maintaining clean separation of concerns.
