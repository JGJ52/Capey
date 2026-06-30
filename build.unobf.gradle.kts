plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
    `maven-publish`
}

version = project.properties["mod_version"] as String
val minecraft_version = project.properties["minecraft_version"] as String
val minecraft_higher_version = project.properties["minecraft_higher_version"] as String
val loader_version = project.properties["loader_version"] as String

val fabricapi_version = project.properties["fabricapi_version"] as String
val screenapi_version = project.properties["screenapi_version"] as String

val mixins_file = project.properties["mixins_file"] as String

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
    implementation("net.fabricmc:fabric-loader:${loader_version}")

    implementation("net.fabricmc.fabric-api:fabric-api:${fabricapi_version}")
    implementation(fletchingTable.modrinth("modmenu", minecraft_version, "fabric"))
    implementation("maven.modrinth:screenapi:${screenapi_version}")
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("minecraft_higher_version", minecraft_higher_version)
    inputs.property("loader_version", loader_version)
    inputs.property("screenapi_version", screenapi_version)
    inputs.property("mixins_file", mixins_file)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "minecraft_version" to minecraft_version,
            "minecraft_higher_version" to minecraft_higher_version,
            "loader_version" to loader_version,
            "screenapi_version" to screenapi_version,
            "mixins_file" to mixins_file
        )
    }
}

val targetJavaVersion = 25
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
        // Add publishing repositories here
    }
}