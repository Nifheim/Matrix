package com.github.beelzebu.matrix.bungee.config;

import com.github.beelzebu.matrix.api.config.MatrixConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfiguration extends MatrixConfig {

    private net.md_5.bungee.config.Configuration config;

    public BungeeConfiguration(File file) {
        super(file);
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    public Collection<String> getKeys(String path) {
        return config.getSection(path).getKeys();
    }

    @Override
    public void reload() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ignored) {
        }
    }
}
