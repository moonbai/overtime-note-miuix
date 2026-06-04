pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    }
}

rootProject.name = "OverTime"
include(":app")
