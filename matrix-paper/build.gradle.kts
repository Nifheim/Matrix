plugins {
    id("io.papermc.paperweight.userdev") version "1.7.4"
}

repositories {
    maven {
        url = uri("https://jitpack.io/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

description = "Matrix Paper"

dependencies {
    implementation(project(":matrix-common"))
    implementation("com.github.nifheim:commandlib:master-SNAPSHOT")
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    implementation("me.clip:placeholderapi:2.11.6")
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

