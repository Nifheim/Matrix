/*
 * This file is part of coins3
 *
 * Copyright © 2020 Beelzebu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.beelzebu.matrix.dependency;

import com.github.beelzebu.matrix.dependency.relodaction.Relocation;
import java.util.Collections;
import java.util.List;

/**
 * @author Beelzebu
 */
public enum Dependency {

    ASM("org.ow2.asm", "asm", "7.1"),
    ASM_COMMONS("org.ow2.asm", "asm-commons", "7.1"),
    JAR_RELOCATOR("me.lucko", "jar-relocator", "1.4"),
    CAFFEINE("com{}github{}ben-manes{}caffeine", "caffeine", "2.8.4", Relocation.of("caffeine", "com{}github{}benmanes{}caffeine")),
    HIKARI("com{}zaxxer", "HikariCP", "3.4.5", Relocation.of("hikari", "com{}zaxxer{}hikari")),
    MARIADB_DRIVER("org{}mariadb{}jdbc", "mariadb-java-client", "2.2.3", Relocation.of("mariadb", "org{}mariadb{}jdbc")),
    COMMONS_POOL_2("org.apache.commons", "commons-pool2", "2.6.2", Relocation.of("commonspool2", "org{}apache{}commons{}pool2")),
    JEDIS("redis.clients", "jedis", "3.3.0", Relocation.allOf(Relocation.of("jedis", "redis{}clients{}jedis"), Relocation.of("jedisutil", "redis{}clients{}util"), Relocation.of("commonspool2", "org{}apache{}commons{}pool2"))),
    MONGODB(
            "org.mongodb",
            "mongo-java-driver",
            "3.12.2"
    ),
    MORPHIA(
            "org.mongodb.morphia",
            "morphia",
            "1.3.2"
    );

    private static final String MAVEN_CENTRAL_FORMAT = "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar";
    private final String url;
    private final String version;
    private final List<Relocation> relocations;

    Dependency(String groupId, String artifactId, String version) {
        this(String.format(MAVEN_CENTRAL_FORMAT, groupId.replace("{}", ".").replace(".", "/"), artifactId.replace("{}", "."), version, artifactId.replace("{}", "."), version), version, Collections.emptyList());
    }

    Dependency(String groupId, String artifactId, String version, Relocation relocations) {
        this(String.format(MAVEN_CENTRAL_FORMAT, groupId.replace("{}", ".").replace(".", "/"), artifactId.replace("{}", "."), version, artifactId.replace("{}", "."), version), version, Collections.singletonList(relocations));
    }

    Dependency(String groupId, String artifactId, String version, List<Relocation> relocations) {
        this(String.format(MAVEN_CENTRAL_FORMAT, groupId.replace("{}", ".").replace(".", "/"), artifactId.replace("{}", "."), version, artifactId.replace("{}", "."), version), version, relocations);
    }

    Dependency(String url, String version, List<Relocation> relocations) {
        this.url = url;
        this.version = version;
        this.relocations = relocations;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public List<Relocation> getRelocations() {
        return relocations;
    }
}
