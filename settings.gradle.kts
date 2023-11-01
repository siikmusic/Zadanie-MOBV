pluginManagement {
    repositories {
        google()
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoieHBhdmxpa3MiLCJhIjoiY2xvZzZpbnJtMTFjeDJzcndnaHd4bW11NSJ9.VenlwafjQRGpoWJvcReAMA"
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
rootProject.name = "Zadanie MOBV"
include(":app")
