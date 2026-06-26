plugins {
    java
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}

tasks.jar {
    archiveFileName.set("DimensionsVelocity-4.0.1.jar")
}
