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
    implementation("io.papermc.paper:paper-api:26.2.build.+")
    implementation("de.rtner:PBKDF2:1.1.4")
    testImplementation(platform("org.junit:junit-bom:5.14.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
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
