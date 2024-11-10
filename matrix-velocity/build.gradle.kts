description = "Matrix Velocity"

repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":matrix-common"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("com.github.games647:craftapi:0.6.2")
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
            include(dependency("com.zaxxer:HikariCP"))
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
            include(dependency("org.apache.commons:commons-pool2"))
            include(dependency("redis.clients:jedis"))
            include(dependency("org.mongodb:bson"))
            include(dependency("org.mongodb:mongodb-driver-core"))
            include(dependency("org.mongodb:mongodb-driver-sync"))
            include(dependency("dev.morphia.morphia:morphia-core"))
        }

        // relocate dependencies to avoid conflicts
        relocate("com.github.games647.craftapi", "net.nifheim.matrix.lib.craftapi")
        relocate("com.zaxxer.hikari", "net.nifheim.matrix.lib.hikari")
        relocate("org.mariadb.jdbc", "net.nifheim.matrix.lib.mariadb")
        relocate("org.apache.commons.pool2", "net.nifheim.matrix.lib.commons.pool2")
        relocate("redis.clients.jedis", "net.nifheim.matrix.lib.jedis")
        relocate("org.mongodb", "net.nifheim.matrix.lib.mongodb")
        relocate("dev.morphia.morphia", "net.nifheim.matrix.lib.morphia")
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

