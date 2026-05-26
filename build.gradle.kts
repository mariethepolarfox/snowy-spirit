import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    idea
    kotlin("jvm") version "2.3.20"
    alias(libs.plugins.kotlin.symbol.processor)
    alias(libs.plugins.loom.unobf)
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
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArg("-Dfabric.modsFolder=" + '"' + rootProject.projectDir.resolve("run/${stonecutter.current.version.replace(".", "")}Mods").absolutePath + '"')
        property("devauth.configDir", rootProject.file(".devauth").absolutePath)
    }
}

tasks {
    processResources {
        val range = if (versionedCatalog.versions.has("minecraft.range")) {
            versionedCatalog.versions.get("minecraft.range").toString()
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
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = preOrPostUnobf(JvmTarget.JVM_21, JvmTarget.JVM_25)
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
    targetCompatibility = preOrPostUnobf(JavaVersion.VERSION_21, JavaVersion.VERSION_25)
    sourceCompatibility = preOrPostUnobf(JavaVersion.VERSION_21, JavaVersion.VERSION_25)
}

kotlin {
    jvmToolchain(preOrPostUnobf(21, 25))
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

fun isPostUnobf(): Boolean = stonecutter.eval(stonecutter.current.version, ">=26.1")

fun <T> preOrPostUnobf(pre: T, post: T): T {
    return if (isPostUnobf()) post else pre
}
