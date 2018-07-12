package io.github.beelzebu.matrix.utils;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.utils.MessagesManager;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class BukkitMessages extends MessagesManager {

    private final MatrixAPI core = MatrixAPI.getInstance();
    private File langFile;
    private YamlConfiguration messages;

    public BukkitMessages(String lang) {
        super(lang);
        langFile = new File(core.getDataFolder(), "messages_" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(core.getDataFolder(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(langFile);
    }

    @Override
    public Object get(String path) {
        return messages.get(path);
    }

    @Override
    public String getString(String path) {
        return messages.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return messages.getStringList(path);
    }

    @Override
    public Boolean getBoolean(String path) {
        return messages.getBoolean(path);
    }

    @Override
    public Integer getInt(String path) {
        return messages.getInt(path);
    }

    @Override
    public Double getDouble(String path) {
        return messages.getDouble(path);
    }

    @Override
    public Object get(String path, Object def) {
        return (messages.get(path) == null ? def : messages.get(path));
    }

    @Override
    public String getString(String path, String def) {
        return (messages.get(path) == null ? def : messages.getString(path));
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return (messages.get(path) == null ? def : messages.getStringList(path));
    }

    @Override
    public Boolean getBoolean(String path, boolean def) {
        return (messages.get(path) == null ? def : messages.getBoolean(path));
    }

    @Override
    public Integer getInt(String path, int def) {
        return (messages.get(path) == null ? def : messages.getInt(path));
    }

    @Override
    public Double getDouble(String path, double def) {
        return (messages.get(path) == null ? def : messages.getDouble(path));
    }

    @Override
    public void set(String path, Object value) {
        messages.set(path, value);
    }

    @Override
    public Collection<String> getKeys(String path) {
        return messages.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void reload() {
        messages = YamlConfiguration.loadConfiguration(langFile);
    }

    @Override
    public List<?> getList(String path) {
        throw new UnsupportedOperationException("getList is not finished yet.");
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        throw new UnsupportedOperationException("getList is not finished yet.");
    }
}
