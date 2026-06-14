pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.myket.ir")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.myket.ir")
        }
    }
}

rootProject.name = "Space Launches Viewer"
include(":app")
include(":data")
include(":domain")
include(":presentation")
