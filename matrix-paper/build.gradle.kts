plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
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
    paperweight.paperDevBundle("26.2.build.+")
    implementation("me.clip:placeholderapi:2.11.6")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

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
            include(dependency("com.github.nifheim:commandlib"))
        }
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

