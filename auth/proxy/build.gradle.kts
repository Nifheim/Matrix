description = "Matrix Auth Proxy"
version = "1.0.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    api(project(":matrix-api"))
    api(project(":matrix-common"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
}

tasks {

    jar {
        archiveBaseName = "matrix-auth-proxy"
        destinationDirectory = file("$rootDir/bin/")
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

