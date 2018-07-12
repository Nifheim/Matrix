package io.github.beelzebu.matrix.utils;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfiguration implements IConfiguration {

    private final File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
    private net.md_5.bungee.config.Configuration config;

    public BungeeConfiguration() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
        }
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
        return config.getSection(path).getKeys();
    }

    @Override
    public void reload() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
        }
    }
}
