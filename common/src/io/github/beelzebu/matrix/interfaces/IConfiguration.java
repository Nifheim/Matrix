package io.github.beelzebu.matrix.interfaces;

import java.util.Collection;
import java.util.List;

public interface IConfiguration {

    Object get(String path);

    String getString(String path);

    List<String> getStringList(String path);

    List<?> getList(String path);

    Boolean getBoolean(String path);

    Integer getInt(String path);

    Double getDouble(String path);

    Object get(String path, Object def);

    String getString(String path, String def);

    List<String> getStringList(String path, List<String> def);

    List<?> getList(String path, List<?> def);

    Boolean getBoolean(String path, boolean def);

    Integer getInt(String path, int def);

    Double getDouble(String path, double def);

    void set(String path, Object value);

    Collection<String> getKeys(String path);

    void reload();
}
