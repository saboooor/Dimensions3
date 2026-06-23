plugins {
    java
}

dependencies {
    compileOnly(project(":"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(project(":custom-lighter"))
    compileOnly("com.github.LoneDev6:api-itemsadder:3.4.1d")
    compileOnly("io.th0rgal:oraxen:1.163.0") {
        isTransitive = false
    }
    compileOnly(files("../custom-lighter/libs/CustomItemsAPI_PLACEHOLDER.jar"))
}

tasks.jar {
    archiveFileName.set("StylishPortals.jar")
}
