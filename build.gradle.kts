import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    idea
    kotlin("jvm") version "2.3.0"
    alias(libs.plugins.kotlin.symbol.processor)
    alias(libs.plugins.loom)
    alias(libs.plugins.auto.mixins)
    `versioned-catalogues`
}

repositories {
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft(versionedCatalog["minecraft"])
    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabricLoader)
    modImplementation(versionedCatalog["fabricApi"])
    modImplementation(libs.fabricKt)

    modImplementation(libs.devauth)

    modImplementation(versionedCatalog["resourcefulconfig"])
    modImplementation(libs.resourcefulconfigkt)
    include(libs.resourcefulconfigkt)

    modImplementation(versionedCatalog["modmenu"])

    include(versionedCatalog["olympus-common"])
    modImplementation(versionedCatalog["olympus-common"])

    include(versionedCatalog["olympus-common"])
    modImplementation(versionedCatalog["olympus-fabric"])
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/snowyspirits.accesswidener")
    runConfigs["client"].apply {
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArg("-Dfabric.modsFolder=" + '"' + rootProject.projectDir.resolve("run/${stonecutter.current.version.replace(".", "")}Mods").absolutePath + '"')
        property("devauth.configDir", rootProject.file(".devauth").absolutePath)
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", versionedCatalog.versions["minecraft"])
        inputs.property("loader_version", libs.versions.fabricLoader.get())

        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to libs.versions.fabricLoader.get(),
                "minecraft_version" to versionedCatalog.versions["minecraft"],
            )
        }
    }

    jar {
        from("LICENSE")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    build {
        doLast {
            val sourceFile = rootProject.projectDir.resolve("versions/${project.name}/build/libs/${stonecutter.current.version}-$version.jar")
            val targetFile = rootProject.projectDir.resolve("build/libs/SnowySpirits-$version-${stonecutter.current.version}.jar")
            targetFile.parentFile.mkdirs()
            targetFile.writeBytes(sourceFile.readBytes())
        }
    }

}

tasks.withType<ValidateAccessWidenerTask> { enabled = false }

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

autoMixins {
    mixinPackage = "me.siv.snowyspirits.mixins"
    projectName = "snowyspirits"
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}