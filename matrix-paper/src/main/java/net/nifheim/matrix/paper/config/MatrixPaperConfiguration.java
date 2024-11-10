package net.nifheim.matrix.paper.config;

import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.MatrixConfiguration;
import net.nifheim.matrix.common.config.adapter.MariaDbConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.MongoConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.RedisConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.ServerInfoConfigurationAdapter;
import net.nifheim.matrix.common.config.sub.MariaDbConfiguration;
import net.nifheim.matrix.common.config.sub.MongoConfiguration;
import net.nifheim.matrix.common.config.sub.RedisConfiguration;
import net.nifheim.matrix.common.config.sub.ServerInfoConfiguration;
import net.nifheim.matrix.paper.MatrixPaper;
import org.jetbrains.annotations.NotNull;

public class MatrixPaperConfiguration implements MatrixConfiguration {

    private final MatrixPaper plugin;
    private MongoConfigurationAdapter mongoConfig;
    private MariaDbConfigurationAdapter mariaDbConfig;
    private RedisConfigurationAdapter redisConfig;
    private ServerInfoConfigurationAdapter serverInfoConfig;

    public MatrixPaperConfiguration(@NotNull MatrixPaper plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
        FileConfigurationWrapper fileConfigurationWrapper = FileConfigurationWrapper.FileConfigurationWrapperImpl.of(path -> plugin.getConfig().get(path));
        mongoConfig = new MongoConfigurationAdapter(fileConfigurationWrapper);
        mariaDbConfig = new MariaDbConfigurationAdapter(fileConfigurationWrapper);
        redisConfig = new RedisConfigurationAdapter(fileConfigurationWrapper);
        serverInfoConfig = new ServerInfoConfigurationAdapter(fileConfigurationWrapper);
    }

    @Override
    public MongoConfiguration getMongoConfig() {
        return mongoConfig;
    }

    @Override
    public MariaDbConfiguration getMariaDbConfig() {
        return mariaDbConfig;
    }

    @Override
    public RedisConfiguration getRedisConfig() {
        return redisConfig;
    }

    @Override
    public ServerInfoConfiguration getServerInfo() {
        return serverInfoConfig;
    }

    @Override
    public boolean isDebug() {
        return plugin.getConfig().getBoolean("debug");
    }
}
