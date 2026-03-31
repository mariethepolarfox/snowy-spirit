import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("idea")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("me.owdding.auto-mixins")
    id("versioned-catalogues")
}

private val stonecutter = project.extensions.getByName("stonecutter") as dev.kikugie.stonecutter.build.StonecutterBuildExtension
fun isUnobfuscated() = stonecutter.eval(stonecutter.current.version, ">=26.1")

fun makeAlias(configuration: String) = if (isUnobfuscated()) configuration else "mod" + configuration.replaceFirstChar { it.uppercase() }

val maybeModImplementation = makeAlias("implementation")
val maybeModCompileOnly = makeAlias("compileOnly")
val maybeModRuntimeOnly = makeAlias("runtimeOnly")
val maybeModApi = makeAlias("api")

repositories {
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    "minecraft"(versionedCatalog["minecraft"])

    maybeModImplementation(versionedCatalog["fabricLoader"])
    maybeModImplementation(versionedCatalog["fabricApi"])
    maybeModImplementation(versionedCatalog["fabricKt"])

    maybeModImplementation(versionedCatalog["devauth"])

    maybeModImplementation(versionedCatalog["resourcefulconfig"])
    maybeModImplementation(versionedCatalog["resourcefulconfigkt"])
    "include"(versionedCatalog["resourcefulconfigkt"])

    maybeModImplementation(versionedCatalog["modmenu"])
}
val accessWidenerFile = rootProject.file(if (isUnobfuscated()) "src/snowyspirits.accesswidener" else "src/snowyspirits.obf.accesswidener")

val loom = extensions.getByName<LoomGradleExtensionAPI>("loom")
loom.apply {
    if (accessWidenerFile.exists()) {
        accessWidenerPath.set(accessWidenerFile)
    }

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
        inputs.property("loader_version", versionedCatalog.versions["fabricLoader"])

        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to versionedCatalog.versions["fabricLoader"],
                "minecraft_version" to versionedCatalog.versions["minecraft"],
            )
        }

        with(copySpec {
            from(accessWidenerFile)
            rename { it.replace(".obf", "") }
        })
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = if (isUnobfuscated()) JvmTarget.JVM_25 else JvmTarget.JVM_21
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
    targetCompatibility = if (isUnobfuscated()) JavaVersion.VERSION_25 else JavaVersion.VERSION_21
    sourceCompatibility = if (isUnobfuscated()) JavaVersion.VERSION_25 else JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(if (isUnobfuscated()) 25 else 21)
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