package com.github.beelzebu.matrix.bukkit.config;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import java.io.File;
import java.util.Collection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class BukkitConfiguration extends MatrixConfig {

    private FileConfiguration config;

    public BukkitConfiguration(@NotNull File file) {
        super(file);
        file.getParentFile().mkdirs();
        if (file.getName().equalsIgnoreCase("config.yml")) {
            config = MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class).getConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }

    @Override
    public @NotNull Object get(@NotNull String path) {
        return config.get(path);
    }

    @Override
    public @NotNull Collection<String> getKeys(@NotNull String path) {
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
    }
}
