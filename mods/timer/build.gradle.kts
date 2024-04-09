import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.13"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
    id("maven-publish")
}

group = "de.derioo.chals.timer"
version = "0.0.0"
description = "Dies ist eine TimerMod, diese Mod nutzt den Befehl <green>'/timer start | stop | reset'</green> und ist standarmäßig aktiviert"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/Knerio/Simple-Chals-Server") {
        credentials {
            username = project.properties["GITHUB_USERNAME"].toString()
            password = project.properties["GITHUB_TOKEN"].toString()
        }
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly("de.derioo.chals:api:0.0.6")
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
        outputJar = layout.buildDirectory.file("libs/Timer.jar")
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
            groupId = "de.derioo.mods"
            artifactId = "timer"
            version = "0.1.5"
            from(components["java"])
            artifact("build/libs/Timer.jar")
        }
    }
}


bukkitPluginYaml {
    main = "de.derioo.chals.timer.Timer"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("Dario")
    depend.add("simpleChalsServer")
    apiVersion = "1.20"
}
