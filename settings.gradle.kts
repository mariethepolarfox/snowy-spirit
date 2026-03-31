
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
    id("dev.kikugie.stonecutter") version "0.9"
}

val versions = listOf("1.21.10", "1.21.11", "26.1")

stonecutter {
    create(rootProject) {
        versions.forEach {
            version(it).buildscript = if (stonecutter.eval(it, "<=1.21.11")) "build.obf.gradle.kts" else "build.gradle.kts"
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
