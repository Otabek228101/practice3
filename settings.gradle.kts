pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KmpPractice"

include(":shared")
include(":androidApp")
include(":structure")
include(":hierarchy")
include(":practice41")
include(":feature:auth")
include(":feature:profile")
include(":umbrella")
