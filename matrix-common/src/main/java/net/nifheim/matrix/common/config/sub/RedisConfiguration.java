package net.nifheim.matrix.common.config.sub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RedisConfiguration {

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
