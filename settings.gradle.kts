rootProject.name = "matrix"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(":matrix-api")
include(":matrix-common")
include(":matrix-velocity")
include(":matrix-paper")
include(":auth:proxy")
include(":auth:paper")
