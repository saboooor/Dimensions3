plugins {
    java
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}

tasks.jar {
    archiveFileName.set("VelocityPlugin.jar")
}
