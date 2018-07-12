package io.github.beelzebu.matrix.utils;

import io.github.beelzebu.matrix.MatrixAPI;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeMessages extends MessagesManager {

    private File langFile;
    private net.md_5.bungee.config.Configuration messages;

    public BungeeMessages(String lang) {
        super(lang);
        langFile = new File(MatrixAPI.getInstance().getDataFolder(), "messages_" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(MatrixAPI.getInstance().getDataFolder(), "messages.yml");
        }
        load(langFile);
        reload();
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
        return messages.getSection(path).getKeys();
    }

    @Override
    public final void reload() {
        load(langFile);
    }

    private net.md_5.bungee.config.Configuration load(File file) {
        try {
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.WARNING, "An unexpected error has ocurred reloading the messages file. {0}", ex.getMessage());
        }
        return messages;
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
