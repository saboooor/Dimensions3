plugins {
    java
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("BungeePlugin-4.0.0.jar")
}
