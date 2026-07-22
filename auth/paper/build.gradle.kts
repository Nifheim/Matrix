description = "Matrix Auth Paper"
version = "1.0.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    api(project(":matrix-api"))
    api(project(":matrix-common"))
    implementation("com.github.nifheim:commandlib:master-SNAPSHOT")
    implementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("de.rtner:PBKDF2:1.1.4")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    jar {
        archiveBaseName = "matrix-auth-paper"
        destinationDirectory = file("$rootDir/bin/")
    }

    processResources {
        expand("pluginVersion" to project.version)
    }

    test {
        useJUnitPlatform()
    }
}
