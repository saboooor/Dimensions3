rootProject.name = "dimensions"
include("patreoncosmetics")
project(":patreoncosmetics").projectDir = file("addons/patreoncosmetics")

include("example-addon")
project(":example-addon").projectDir = file("addons/example-addon")

include("force-link")
project(":force-link").projectDir = file("addons/force-link")

include("horizontal-portals")
project(":horizontal-portals").projectDir = file("addons/horizontal-portals")

include("worldguard-flags")
project(":worldguard-flags").projectDir = file("addons/worldguard-flags")
