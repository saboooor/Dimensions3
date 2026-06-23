plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.ticxo.modelengine:api:R3.0.0")
    compileOnly("me.clip:placeholderapi:2.11.2")
}

tasks.jar {
    archiveFileName.set("ModelEngineAddon.jar")
}
