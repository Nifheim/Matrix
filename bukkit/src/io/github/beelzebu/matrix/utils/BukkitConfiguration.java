package io.github.beelzebu.matrix.utils;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import java.util.Collection;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

public class BukkitConfiguration implements IConfiguration {

    private final Main plugin;
    private final FileConfiguration config;

    public BukkitConfiguration(Main main) {
        plugin = main;
        config = main.getConfig();
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    @Override
    public List<?> getList(String path) {
        return config.getList(path);
    }

    @Override
    public Boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    @Override
    public Integer getInt(String path) {
        return config.getInt(path);
    }

    @Override
    public Double getDouble(String path) {
        return config.getDouble(path);
    }

    @Override
    public Object get(String path, Object def) {
        return (config.get(path) == null ? def : config.get(path));
    }

    @Override
    public String getString(String path, String def) {
        return (config.get(path) == null ? def : config.getString(path));
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return (config.get(path) == null ? def : config.getStringList(path));
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return (config.get(path) == null ? def : config.getList(path));
    }

    @Override
    public Boolean getBoolean(String path, boolean def) {
        return (config.get(path) == null ? def : config.getBoolean(path));
    }

    @Override
    public Integer getInt(String path, int def) {
        return (config.get(path) == null ? def : config.getInt(path));
    }

    @Override
    public Double getDouble(String path, double def) {
        return (config.get(path) == null ? def : config.getDouble(path));
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, value);
    }

    @Override
    public Collection<String> getKeys(String path) {
        return config.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
    }
}
