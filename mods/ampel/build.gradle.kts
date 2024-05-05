import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import java.util.*

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.7.0"
  id("xyz.jpenilla.run-paper") version "2.3.0"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
  id("maven-publish")
}

group = "de.derioo.chals.ampel"
version = "0.0.0"
description = "Dies ist eine Ampel Mod, beim Aktivieren ist sie sofort aktiv!"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
  maven("https://maven.pkg.github.com/Knerio/SimpleChals") {
    credentials {
      username = project.properties["GITHUB_USERNAME"].toString()
      password = project.properties["GITHUB_TOKEN"].toString()
    }
  }
}

dependencies {
  paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
  compileOnly("de.derioo.chals:api:0.2.10")
  compileOnly("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
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
    outputJar = layout.buildDirectory.file("libs/Ampel.jar")
  }

  publish {
    mustRunAfter("reobfJar")
  }

}

fun ver(): String {
  val rootProjectDir = file("../..")
  val gradlePropertiesFile = rootProjectDir.resolve("gradle.properties")
  val gradleProperties = Properties()
  gradleProperties.load(gradlePropertiesFile.inputStream())
  return gradleProperties["VERSION"].toString();
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/Knerio/SimpleChals")
      credentials {
        username = project.properties["GITHUB_USERNAME"].toString()
        password = project.properties["GITHUB_TOKEN"].toString()
      }
    }
  }
  publications {
    register<MavenPublication>("gpr") {
      groupId = "de.derioo.mods"
      artifactId = "ampel"
      version = ver()
      from(components["java"])
      artifact("build/libs/Ampel.jar")
    }
  }
}


bukkitPluginYaml {
  main = "de.derioo.chals.ampel.Ampel"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("Dario")
  depend.add("SC-Server")
  apiVersion = "1.20"
}
