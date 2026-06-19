plugins {
    java
    id("com.diffplug.spotless") version "8.7.0"
}

group = "me.xxastaspastaxx"
version = "4.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "codemc-releases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.0")
}

spotless {
    java {
        // Fixes trailing spaces, line endings, and multiple empty lines
        trimTrailingWhitespace()
        endWithNewline()
        
        // Maps to your 'ignores' array
        targetExclude("**/build/**", "**/target/**", "**/bin/**")

        // Enforces Google Java Format (2-space indent, strict bracket/curly spacing)
        googleJavaFormat("1.35.0").reflowLongStrings()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    archiveFileName.set("dimensions-${project.version}.jar")
}
