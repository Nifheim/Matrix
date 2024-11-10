plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

description = "Matrix Paper"

dependencies {
    implementation(project(":matrix-common"))
    implementation("net.nifheim:commandlib:1.1-SNAPSHOT")
    implementation("net.nifheim:coreutils:1.2.0-SNAPSHOT")
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    implementation("me.clip:placeholderapi:2.11.2")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    artifacts {
        archives(shadowJar)
    }

    assemble {
        dependsOn(reobfJar)
    }

    shadowJar {
        archiveClassifier = null
        archiveVersion = null
        destinationDirectory = file("$rootDir/bin/")

        dependencies {
            include(project(":matrix-api"))
            include(project(":matrix-common"))
            include(dependency("net.nifheim:coreutils"))
            include(dependency("net.nifheim:commandlib"))
        }
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

