buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("idea")
}

allprojects {

    group = "net.nifheim"
    version = "2.0.0-SNAPSHOT"

    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "idea")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains:annotations:24.1.0")
        implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    idea {
        module {
            isDownloadJavadoc = true
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
            dependsOn(clean)
        }

        build {
            dependsOn(shadowJar)
        }

        clean {
            doLast {
                file("${rootDir}/bin").delete()
            }
        }
    }
}
