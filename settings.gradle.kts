pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
        maven {
            name = "KikuGie"
            url = uri("https://maven.kikugie.dev/snapshots")
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    centralScript = "build.unobf.gradle.kts"

    create(getRootProject()) {
        versions("26.2", "26.1", "1.21.11", "1.21.9")
        versions("1.21.11", "1.21.9").buildscript("build.obf.gradle.kts")
    }
}