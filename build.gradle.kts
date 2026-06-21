import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    idea
    kotlin("jvm") version libs.versions.kotlin.version.get()
    alias(libs.plugins.kotlin.symbol.processor)
    alias(libs.plugins.loom)
    alias(libs.plugins.auto.mixins)
    alias(libs.plugins.mod.publishing)
    `versioned-catalogues`
}

repositories {
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft(versionedCatalog["minecraft"])

    implementation(libs.fabricLoader)
    implementation(versionedCatalog["fabricApi"])
    implementation(libs.fabricKt)

    implementation(libs.devauth)

    implementation(versionedCatalog["resourcefulconfig"])
    implementation(versionedCatalog["resourcefulconfigkt"])
    include(versionedCatalog["resourcefulconfigkt"])

    implementation(versionedCatalog["modmenu"])

    include(versionedCatalog["olympus"])
    implementation(versionedCatalog["olympus"])
}

var accessWidenerFile = rootProject.file("src/snowyspirits.accesswidener")
loom {
    accessWidenerPath = accessWidenerFile
    runConfigs["client"].apply {
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dfabric.modsFolder=" + '"' + rootProject.projectDir.resolve("run/${stonecutter.current.version.replace(".", "")}Mods").absolutePath + '"')
        systemProperties.put("devauth.configDir", rootProject.file(".devauth").absolutePath)
    }
}

tasks {
    processResources {
        val range = if (versionedCatalog.versions.has("minecraft.range")) {
            versionedCatalog.versions["minecraft.range"].toString()
        } else {
            val start = versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft")
            val end = versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft")
            ">=$start <=$end"
        }
        inputs.property("version", project.version)
        inputs.property("minecraft_version", range)
        inputs.property("loader_version", libs.versions.fabricLoader.get())

        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to libs.versions.fabricLoader.get(),
                "minecraft_version" to range,
            )
        }

        with(copySpec {
            from(accessWidenerFile)
        })
    }

    jar {
        from("LICENSE")
        archiveFileName.set("SnowySpirits-$version-${stonecutter.current.version}.jar")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
        }
    }

    build {
        doLast {
            val sourceFile = rootProject.projectDir.resolve("versions/${project.name}/build/libs/SnowySpirits-$version-${stonecutter.current.version}.jar")
            val targetFile = rootProject.projectDir.resolve("build/libs/SnowySpirits-$version-${stonecutter.current.version}.jar")
            targetFile.parentFile.mkdirs()
            targetFile.writeBytes(sourceFile.readBytes())
        }
    }

}

tasks.withType<ValidateAccessWidenerTask> { enabled = false }

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_25
    sourceCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
}

autoMixins {
    mixinPackage = "me.marie.snowyspirits.mixins"
    projectName = "snowyspirits"
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

publishMods {
    val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
    val ver = project.version

    var changelogText = rootProject.file("CHANGELOG.md").readText()
    val replacements = mapOf(
        "%version%" to ver.toString(),
    )

    replacements.forEach { (placeholder, value) ->
        changelogText = changelogText.replace(placeholder, value)
    }

    displayName.set("SnowySpirits $ver")
    changelog.set(changelogText)

    file.set(tasks.jar.get().archiveFile)
    type.set(BETA)
    modLoaders.add("fabric")

    modrinth {
        accessToken.set(modrinthToken)
        projectId.set("hAEGcgZe")

        if (versionedCatalog.versions.has("minecraft.range")) {
            minecraftVersions.add(versionedCatalog.versions["minecraft.range"].toString())
        } else {
            minecraftVersionRange {
                start.set(versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft").toString())
                end.set(versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft").toString())
            }
        }

        requires("fabric-api")
        requires("fabric-language-kotlin")

        optional("modmenu")
    }

    dryRun.set(modrinthToken.orNull == null)
}
