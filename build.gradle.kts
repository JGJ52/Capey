plugins {
    id("dev.kikugie.loom-back-compat") version "0.4.1"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
    id("me.modmuss50.mod-publish-plugin") version "2.1.1"
    `maven-publish`
}

val minecraft_version = project.properties["minecraft_version"] as String
version = project.properties["mod_version"] as String + "+" + minecraft_version
val minecraft_higher_version = project.properties["minecraft_higher_version"] as String
val loader_version = project.properties["loader_version"] as String

val screenapi_version = project.properties["screenapi_version"] as String + "+" + project.properties["screenapi_mc_version"] as String

group = project.properties["maven_group"] as String

stonecutter {
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
    }
    replacements.string(current.parsed >= "26.1") {
        replace("GuiGraphics", "GuiGraphicsExtractor")
    }
}

base {
    archivesName.set(project.properties["archives_base_name"] as String)
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    modImplementation(fletchingTable.modrinth("modmenu", minecraft_version, "fabric"))
    modImplementation("maven.modrinth:screenapi:${screenapi_version}")
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

    filesMatching("mixins.json") {
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
        rename { "${it}_${project.properties["archives_base_name"]}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.properties["archives_base_name"] as String
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

publishMods {
    file.set(tasks.jar.flatMap { it.archiveFile })
    type.set(STABLE)
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