plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(project(":particles"))
}

tasks.jar {
    archiveFileName.set("TimedPortals-4.0.0.jar")
}
