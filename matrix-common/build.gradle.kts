dependencies {
    api(project(":matrix-api"))
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.3")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("redis.clients:jedis:3.9.0")
    implementation("org.mongodb:bson:4.11.1")
    implementation("org.mongodb:mongodb-driver-core:4.11.1")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("dev.morphia.morphia:morphia-core:2.4.12")
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.15.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.15.0")
    implementation("com.google.code.gson:gson:2.8.9")
}

description = "Matrix Common"

repositories {
    mavenCentral()
}
