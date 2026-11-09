pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
        maven {
            name = "KikuGie Snapshots"
            url = uri("https://maven.kikugie.dev/snapshots")
        }
        maven {
            name = "KikuGie Releases"
            url = uri("https://maven.kikugie.dev/releases")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.kikugie.dev/snapshots")
    }

    versionCatalogs {
        create("ft") { from("dev.kikugie.fletching-table:fletching-table.catalog:0.2-SNAPSHOT") }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.10"
    id("dev.kikugie.loom-back-compat") version "0.4.3"
}

stonecutter {
    centralScript = "build.gradle.kts"

    create(getRootProject()) {
        versions("26.2", "26.1", "1.21.11", "1.21.9")
    }
}