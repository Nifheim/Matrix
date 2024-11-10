description = "Matrix Bungee"

repositories {
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-releases/")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }

    maven {
        url = uri("https://repo.nukkitx.com/maven-snapshots")
    }

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":matrix-common"))
    implementation("io.github.waterfallmc:waterfall-api:1.20-R0.1-SNAPSHOT")
    implementation("com.github.games647:craftapi:0.4")
    implementation("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bungeecord:4.3.0")
}

tasks {
    artifacts {
        archives(shadowJar)
    }

    shadowJar {
        archiveClassifier = null
        archiveVersion = null
        destinationDirectory = file("$rootDir/bin/")

        dependencies {
            include(project(":matrix-api"))
            include(project(":matrix-common"))
            include(dependency("com.github.games647:craftapi"))
            include(dependency("net.kyori:adventure-api"))
        }
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

