plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.github.Lauriethefish:BetterPortals:0.8.0")
    compileOnly(project(":horizontal-portals"))
}

tasks.jar {
    archiveFileName.set("BetterPortals-4.0.0.jar")
}
