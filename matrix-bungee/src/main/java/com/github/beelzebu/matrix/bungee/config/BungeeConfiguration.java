package com.github.beelzebu.matrix.bungee.config;

import com.github.beelzebu.matrix.api.config.MatrixConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull Object get(@NotNull String path) {
        return config.get(path);
    }

    @Override
    public @NotNull Collection<String> getKeys(@NotNull String path) {
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
