import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.5.13"
  id("xyz.jpenilla.run-paper") version "2.2.3"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
  id("maven-publish")
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.derioo.chals.server"
version = "0.0.0"
description = "Backend Server Mod Wrapper"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
  maven {
    name = "derioReleases"
    url = uri("https://nexus.derioo.de/releases")
  }
}


dependencies {
  paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

  compileOnly("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
  testCompileOnly("org.projectlombok:lombok:1.18.32")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

  implementation("com.github.stefvanschie.inventoryframework:IF:0.10.13")
  implementation("de.derioo:inventoryframework:5.0.0")

}

tasks {
  assemble {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()

    options.release = 17
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  reobfJar {
    outputJar = layout.buildDirectory.file("libs/SCS.jar")
  }
  publish {
    mustRunAfter("reobfJar")
  }
}



publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/Knerio/Simple-Chals-Server")
      credentials {
        username = project.properties["GITHUB_USERNAME"].toString()
        password = project.properties["GITHUB_TOKEN"].toString()
      }
    }
  }
  publications {
    register<MavenPublication>("gpr") {
      groupId = "de.derioo.chals"
      artifactId = "api"
      version = "0.1.17"
      from(components["java"])
      artifact("build/libs/SCS.jar")
    }
  }
}

bukkitPluginYaml {
  main = "de.derioo.chals.server.ServerCore"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("Dario")
  apiVersion = "1.20"
}
