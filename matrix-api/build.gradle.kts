description = "Matrix API"

plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // slf4j
    implementation("org.slf4j:slf4j-api:1.7.32")
    // gson
    implementation("com.google.code.gson:gson:2.8.9")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
