plugins {
    java
    id("com.diffplug.spotless") version "8.7.0" apply false
}

group = "me.xxastaspastaxx"
version = "4.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

// Repositories are configured globally in the allprojects block below

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.0")
}

allprojects {
    apply(plugin = "com.diffplug.spotless")
    
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "enginehub"
            url = uri("https://maven.enginehub.org/repo/")
        }
        maven {
            name = "bg-software"
            url = uri("https://repo.bg-software.com/repository/api/")
        }
        maven {
            name = "codemc"
            url = uri("https://repo.codemc.org/repository/maven-public/")
        }

        maven {
            name = "lumine"
            url = uri("https://mvn.lumine.io/repository/maven-public/")
        }
        maven {
            name = "placeholderapi"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        maven {
            name = "placeholderapi-releases"
            url = uri("https://repo.extendedclip.com/releases/")
        }
        maven {
            name = "helpchat"
            url = uri("https://repo.helpch.at/releases")
        }
        maven {
            name = "spigot"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            name = "bungeecord"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            trimTrailingWhitespace()
            endWithNewline()
            targetExclude("**/build/**", "**/target/**", "**/bin/**")
            googleJavaFormat("1.35.0").reflowLongStrings()
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    archiveFileName.set("dimensions-${project.version}.jar")
}
