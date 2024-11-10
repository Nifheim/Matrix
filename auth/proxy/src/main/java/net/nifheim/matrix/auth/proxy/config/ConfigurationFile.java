package net.nifheim.matrix.auth.proxy.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class ConfigurationFile {

    private final Path file;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode root;

    public ConfigurationFile(Path file) throws ConfigurateException {
        this.file = file;
        this.loader = YamlConfigurationLoader.builder().path(file).indent(2).nodeStyle(NodeStyle.BLOCK).build();
        root = loader.load();
    }

    public void reload() throws ConfigurateException {
        root = loader.load();
    }

    public void save() throws ConfigurateException {
        loader.save(root);
    }

    public ConfigurationNode getRoot() {
        return root;
    }

    public Path getFile() {
        return file;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
        return loader;
    }

    public String getString(String path) {
        return root.node(path(path)).getString();
    }

    public List<String> getStringList(String path) throws SerializationException {
        return root.node(path(path)).getList(String.class);
    }

    public int getInt(String path) {
        return root.node(path(path)).getInt();
    }

    public boolean getBoolean(String path) {
        return root.node(path(path)).getBoolean();
    }

    public void set(String path, Object value) throws SerializationException {
        root.node(path(path)).set(value);
    }

    public void remove(String path) throws SerializationException {
        root.node(path(path)).set(null);
    }

    private List<String> path(String path) {
        return Arrays.asList(path.split("\\."));
    }
}
