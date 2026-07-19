pluginManagement {
    includeBuild("build-logic")
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BibleLib"

// App shell
include(":app")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:ui")
include(":core:designsystem")

// Feature modules
include(":feature:selection")
include(":feature:reader")
include(":feature:bookmarknotes")
include(":feature:search")
include(":feature:scriptureopener")
include(":feature:history")
include(":feature:settings")
include(":feature:bibles")
include(":feature:help")
include(":feature:donation")
