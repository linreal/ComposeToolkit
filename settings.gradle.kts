pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Compose Toolkit"
include(":app")
include(":logging-annotations")
include(":compiler-plugin:logger:plugin")
include(":compiler-plugin:logger:runtime")
include(":compiler-plugin:recomposition-tracker:plugin")
include(":compiler-plugin:recomposition-tracker:runtime")
include(":gradle-plugin:sub-plugin:logger")
include(":gradle-plugin:sub-plugin:recomposition-tracker")
include(":gradle-plugin:toolkit")
 
