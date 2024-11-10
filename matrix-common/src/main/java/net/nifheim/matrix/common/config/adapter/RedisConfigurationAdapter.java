package net.nifheim.matrix.common.config.adapter;

import java.util.Objects;
import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.sub.RedisConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter class to convert a {@link FileConfigurationWrapper} to a {@link RedisConfiguration}.
 *
 * @author Jaime Suárez
 */
public class RedisConfigurationAdapter implements RedisConfiguration {

    private final FileConfigurationWrapper config;

    public RedisConfigurationAdapter(@NotNull FileConfigurationWrapper config) {
        this.config = config;
    }

    @Override
    public @NotNull String getHost() {
        return Objects.requireNonNull(config.getString("redis.host"));
    }

    @Override
    public int getPort() {
        return config.getInt("redis.port", 6379);
    }

    @Override
    public @Nullable String getPassword() {
        return config.getString("redis.password");
    }

    @Override
    public int getDatabase() {
        return config.getInt("redis.database");
    }

    @Override
    public int getMinIdle() {
        return config.getInt("redis.min-idle", 1);
    }

    @Override
    public int getMaxIdle() {
        return config.getInt("redis.max-idle", 30);
    }

    @Override
    public int getMaxActive() {
        return config.getInt("redis.max-active", 30);
    }

    @Override
    public boolean isTestOnBorrow() {
        return config.getBoolean("redis.test-on-borrow", true);
    }

    @Override
    public boolean isTestWhileIdle() {
        return config.getBoolean("redis.test-while-idle", true);
    }

    @Override
    public boolean isBlockWhenExhausted() {
        return config.getBoolean("redis.block-when-exhausted", true);
    }
}
