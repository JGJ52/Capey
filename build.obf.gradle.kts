plugins {
    id("fabric-loom") version "1.16-SNAPSHOT"
    `maven-publish`
}

version = project.properties["mod_version"] as String
val minecraft_version = project.properties["minecraft_version"] as String
val minecraft_higher_version = project.properties["minecraft_higher_version"] as String
val loader_version = project.properties["loader_version"] as String

val fabric_version = project.properties["fabric_version"] as String
val screenapi_version = project.properties["screenapi_version"] as String
val modmenu_version = project.properties["modmenu_version"] as String

val mixins_file = project.properties["mixins_file"] as String

group = project.properties["maven_group"] as String

base {
    archivesName.set(project.properties["archives_base_name"] as String)
}

repositories {
    maven { url = uri("https://maven.terraformersmc.com/releases") }
    maven { url = uri("https://api.modrinth.com/maven") }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("maven.modrinth:screenapi:${screenapi_version}")
    modImplementation("com.terraformersmc:modmenu:${modmenu_version}")
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

val targetJavaVersion = 21
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