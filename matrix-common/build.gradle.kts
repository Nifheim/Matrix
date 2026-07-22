dependencies {
    api(project(":matrix-api"))
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.3")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("redis.clients:jedis:3.9.0")
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.15.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.15.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.rabbitmq:amqp-client:5.27.0")
}

description = "Matrix Common"

repositories {
    mavenCentral()
}
