import dev.kikugie.fletching_table.mod.ModLoader

plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    alias(ft.plugins.default)
    `maven-publish`
}

val minecraft_version = project.property("minecraft_version") as String
version = project.property("mod_version") as String + "+" + minecraft_version
val minecraft_higher_version = project.property("minecraft_higher_version") as String
val loader_version = project.property("loader_version") as String

val screenapi_version = project.property("screenapi_version") as String + "+" + project.property("screenapi_mc_version") as String

val archives_base_name = project.property("archives_base_name") as String

group = project.property("maven_group") as String

stonecutter {
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
    }
    replacements.string(current.parsed >= "26.1") {
        replace("GuiGraphics", "GuiGraphicsExtractor")
    }
}

fletchingTable {
    lang.configure(sourceSets.main) {}
}

base {
    archivesName.set(archives_base_name)
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven("https://maven.jgj52.dev/releases")
}

dependencies {
    fletchingTable.loader = ModLoader.FABRIC
    fletchingTable.minecraft = minecraft_version

    minecraft("com.mojang:minecraft:${minecraft_version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    modImplementation(fletchingTable.modrinth("modmenu")!!)
    modImplementation("dev.jgj52:screenapi:${screenapi_version}")
}

val targetJavaVersion = when {
    sc.current.parsed >= "26.1" -> 25
    else -> 21
}
tasks.processResources {
    inputs.property("version", version)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("minecraft_higher_version", minecraft_higher_version)
    inputs.property("loader_version", loader_version)
    inputs.property("screenapi_version", screenapi_version)

    inputs.property("java_version", "JAVA_${targetJavaVersion}")
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "minecraft_version" to minecraft_version,
            "minecraft_higher_version" to minecraft_higher_version,
            "loader_version" to loader_version,
            "screenapi_version" to screenapi_version
        )
    }

    filesMatching("${archives_base_name}.mixins.json") {
        expand(
            "java_version" to "JAVA_${targetJavaVersion}"
        )
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archives_base_name}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Reposilite"
            url = uri("https://maven.jgj52.dev/releases")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

val jt = when {
    sc.current.parsed >= "26.1" -> tasks.named<AbstractArchiveTask>("jar")
    else -> tasks.named<AbstractArchiveTask>("remapJar")
}

publishMods {
    file.set(jt.flatMap { it.archiveFile })
    type.set(ALPHA)
    changelog.set("")
    modLoaders.add("fabric")

    modrinth {
        projectId.set(System.getenv("MODRINTH_ID"))
        accessToken.set(System.getenv("MODRINTH_TOKEN"))

        environment.set(CLIENT_ONLY)

        minecraftVersionRange {
            start.set(minecraft_version)
            end.set(minecraft_higher_version)
        }

        requires("screenapi", "modmenu")
    }
}