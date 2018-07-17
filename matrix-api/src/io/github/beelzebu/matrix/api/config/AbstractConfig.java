package io.github.beelzebu.matrix.api.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public abstract class AbstractConfig {

    protected final File file;

    public abstract Object get(String path);

    public Object get(String path, Object def) {
        return get(path) != null ? get(path) : def;
    }

    public String getString(String path) {
        return get(path) instanceof String ? (String) get(path) : null;
    }

    public String getString(String path, String def) {
        return getString(path) != null ? getString(path) : def;
    }

    public int getInt(String path) {
        return get(path, -1) instanceof Number ? ((Number) get(path, -1)).intValue() : -1;
    }


    public int getInt(String path, int def) {
        return get(path, def) instanceof Number ? ((Number) get(path, def)).intValue() : def;
    }

    public double getDouble(String path) {
        return get(path, -1) instanceof Number ? ((Number) get(path, -1)).doubleValue() : -1;
    }

    public double getDouble(String path, double def) {
        return get(path, def) instanceof Number ? ((Number) get(path, def)).intValue() : def;
    }

    public boolean getBoolean(String path) {
        return get(path, false) instanceof Boolean && (boolean) get(path, false);
    }

    public boolean getBoolean(String path, boolean def) {
        return get(path, def) instanceof Boolean && (boolean) get(path, def);
    }

    public List<?> getList(String path) {
        return get(path) instanceof List ? (List<?>) get(path) : new ArrayList<>();
    }

    public List<String> getStringList(String path) {
        return getList(path).get(0) instanceof String ? (List<String>) getList(path) : new ArrayList<>();
    }

    public abstract Collection<String> getKeys(String path);

    public abstract void reload();
}
