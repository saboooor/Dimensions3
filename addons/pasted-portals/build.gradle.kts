plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2024.4")
    compileOnly("world.bentobox:bentobox:1.17.2-SNAPSHOT")
    compileOnly("com.iridium:IridiumSkyblock:4.1.4")
}

tasks.jar {
    archiveFileName.set("PastedPortals.jar")
}
