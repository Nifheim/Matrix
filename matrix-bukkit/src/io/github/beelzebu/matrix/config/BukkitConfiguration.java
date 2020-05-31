package io.github.beelzebu.matrix.config;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import java.io.File;
import java.util.Collection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BukkitConfiguration extends MatrixConfig {

    private FileConfiguration config;

    public BukkitConfiguration(File file) {
        super(file);
        file.getParentFile().mkdirs();
        if (file.getName().equalsIgnoreCase("config.yml")) {
            config = MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class).getConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    public Collection<String> getKeys(String path) {
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
