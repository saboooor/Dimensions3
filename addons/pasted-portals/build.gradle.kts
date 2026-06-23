plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:latest")
    compileOnly("world.bentobox:bentobox:1.17.2")
    compileOnly("com.iridium:IridiumSkyblock:3.0.8")
}

tasks.jar {
    archiveFileName.set("PastedPortals.jar")
}
