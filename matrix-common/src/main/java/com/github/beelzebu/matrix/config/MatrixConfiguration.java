package com.github.beelzebu.matrix.config;

import com.github.beelzebu.matrix.api.server.ServerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MatrixConfiguration {


    RedisConfiguration getRedisConfig();

    ServerInfo getServerInfo();

    boolean isDebug();

    interface RedisConfiguration {

        @NotNull String getHost();

        int getPort();

        @Nullable String getPassword();

        int getDatabase();

        int getMinIdle();

        int getMaxIdle();

        int getMaxActive();

        boolean isTestOnBorrow();

        boolean isTestWhileIdle();

        boolean isBlockWhenExhausted();
    }
}
