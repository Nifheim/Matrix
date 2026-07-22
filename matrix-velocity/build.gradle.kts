description = "Matrix Velocity"

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":matrix-common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
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
            // com.rabbitmq:amqp-client:5.27.0
            include(dependency("com.rabbitmq:amqp-client"))
        }

        // relocate dependencies to avoid conflicts
        relocate("com.github.games647.craftapi", "net.nifheim.matrix.lib.craftapi")
        relocate("com.zaxxer.hikari", "net.nifheim.matrix.lib.hikari")
        relocate("org.mariadb.jdbc", "net.nifheim.matrix.lib.mariadb")
        relocate("org.apache.commons.pool2", "net.nifheim.matrix.lib.commons.pool2")
        relocate("redis.clients.jedis", "net.nifheim.matrix.lib.jedis")
        relocate("com.rabbitmq.client", "net.nifheim.matrix.lib.rabbitmq")
    }

    processResources {
        expand("pluginVersion" to project.version)
    }
}

