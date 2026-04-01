
rootProject.name = "Snowy Spirits"

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.teamresourceful.com/repository/maven-public/")
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.1-beta.2"
}

val versions = listOf("26.1", "1.21.11")

stonecutter {
    create(rootProject) {
        versions.forEach { version ->
            version(version).buildscript = if (stonecutter.eval(version, ">=26.1")) "build.gradle.kts" else "build.obf.gradle.kts"
        }
        vcsVersion = versions.first()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        versions.forEach { version ->
            val versionName = version.replace('.', '_')
            create("libs${versionName.replace("_", "")}") {
                from(
                    files(
                        rootProject.projectDir.resolve("gradle/$versionName.versions.toml")
                    )
                )
            }
        }
    }
}
