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
        mavenLocal()
        mavenCentral()

        maven {
            url = uri("https://repo.maven.apache.org/maven2")
        }

        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

        maven {
            url = uri("https://repo.md-5.net/content/repositories/snapshots/")
        }

        maven {
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }

        maven {
            url = uri("https://repo.destroystokyo.com/repository/maven-snapshots/")
        }

        maven {
            url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        }

        maven {
            url = uri("https://jitpack.io/")
        }
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
