package com.github.beelzebu.matrix.bukkit.config;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.config.MatrixConfiguration;
import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitConfiguration extends MatrixConfig implements MatrixConfiguration {

    private FileConfiguration config;
    private RedisConfiguration redisConfig;
    private ServerInfoConfigurationAdapter serverInfoConfig;

    public BukkitConfiguration(@NotNull File file) {
        super(file);
        file.getParentFile().mkdirs();
        reload();
    }

    @Override
    public @Nullable Object get(@NotNull String path) {
        return config.get(path);
    }

    @Override
    public @Nullable Collection<String> getKeys(@NotNull String path) {
        return config.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void reload() {
        if (file.getName().equalsIgnoreCase("config.yml")) {
            MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class).reloadConfig();
            config = MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class).getConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
        redisConfig = new RedisConfigurationAdapter(config);
        serverInfoConfig = new ServerInfoConfigurationAdapter(config);
    }

    @Override
    public RedisConfiguration getRedisConfig() {
        return redisConfig;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfoConfig;
    }

    @Override
    public boolean isDebug() {
        return config.getBoolean("debug");
    }

    /**
     * Adapter class to convert Bukkit's {@link FileConfiguration} to {@link RedisConfiguration}.
     *
     * @author Jaime Suárez
     */
    private class RedisConfigurationAdapter implements RedisConfiguration {

        private final FileConfiguration config;

        public RedisConfigurationAdapter(@NotNull FileConfiguration config) {
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

    /**
     * Adapter class to convert Bukkit's {@link FileConfiguration} to {@link ServerInfo}.
     *
     * @author Jaime Suárez
     */
    private class ServerInfoConfigurationAdapter extends ServerInfo {

        private FileConfiguration config;

        public ServerInfoConfigurationAdapter(@NotNull FileConfiguration config) {
            this.config = config;
        }

        @Override
        public @Nullable GameMode getDefaultGameMode() {
            return config.isSet("server-info.game-mode") ? GameMode.valueOf(config.getString("server-info.game-mode").toUpperCase()) : null;
        }

        @Override
        public @NotNull String getGroupName() {
            return config.getString("server-info.group-name");
        }

        @Override
        public @NotNull String getServerName() {
            return config.getString("server-info.server-name");
        }

        @Override
        public @NotNull CompletableFuture<String> getLobbyServer() {
            return CompletableFuture.completedFuture(config.getString("server-info.lobby-server"));
        }

        @Override
        public boolean isUnique() {
            return config.getBoolean("server-info.unique");
        }

        @Override
        public @NotNull ServerType getServerType() {
            return config.isSet("server-info.server-type") ? ServerType.valueOf(config.getString("server-info.server-type").toUpperCase()) : ServerType.SURVIVAL;
        }
    }
}
