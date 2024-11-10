package net.nifheim.matrix.velocity.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.MatrixConfiguration;
import net.nifheim.matrix.common.config.adapter.MariaDbConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.MongoConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.RedisConfigurationAdapter;
import net.nifheim.matrix.common.config.adapter.ServerInfoConfigurationAdapter;
import net.nifheim.matrix.common.config.sub.MariaDbConfiguration;
import net.nifheim.matrix.common.config.sub.MongoConfiguration;
import net.nifheim.matrix.common.config.sub.RedisConfiguration;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class MatrixVelocityConfiguration implements MatrixConfiguration {

    private final YamlConfigurationLoader loader;
    private ConfigurationNode root;
    private MongoConfigurationAdapter mongoConfigurationAdapter;
    private MariaDbConfigurationAdapter mariaDbConfigurationAdapter;
    private RedisConfigurationAdapter redisConfigurationAdapter;
    private ServerInfoConfigurationAdapter serverInfoConfigurationAdapter;
    private final FileConfigurationWrapper fileConfigurationWrapper = FileConfigurationWrapper.FileConfigurationWrapperImpl.of(path -> {
        try {
            return root.node(Arrays.asList(path.split("\\."))).get(Object.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    });

    public MatrixVelocityConfiguration(File file) {
        loader = YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).file(file).build();
        reload();
    }

    @Override
    public void reload() {
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mongoConfigurationAdapter = new MongoConfigurationAdapter(fileConfigurationWrapper);
        mariaDbConfigurationAdapter = new MariaDbConfigurationAdapter(fileConfigurationWrapper);
        redisConfigurationAdapter = new RedisConfigurationAdapter(fileConfigurationWrapper);
        serverInfoConfigurationAdapter = new ServerInfoConfigurationAdapter(fileConfigurationWrapper);
    }

    @Override
    public MongoConfiguration getMongoConfig() {
        return mongoConfigurationAdapter;
    }

    @Override
    public MariaDbConfiguration getMariaDbConfig() {
        return mariaDbConfigurationAdapter;
    }

    @Override
    public RedisConfiguration getRedisConfig() {
        return redisConfigurationAdapter;
    }

    @Override
    public ServerInfoConfigurationAdapter getServerInfo() {
        return serverInfoConfigurationAdapter;
    }

    @Override
    public boolean isDebug() {
        return fileConfigurationWrapper.getBoolean("debug");
    }
}
