package io.github.beelzebu.matrix.config;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import java.io.File;
import java.util.Collection;

/**
 * @author Beelzebu
 */
public class ExternalConfig extends AbstractConfig {

    private final Main main;

    public ExternalConfig(Main main, File file) {
        super(file);
        this.main = main;
    }

    @Override
    public Object get(String path) {
        return main.getConfig().get(path);
    }

    @Override
    public Collection<String> getKeys(String path) {
        return main.getConfig().keySet();
    }

    @Override
    public void reload() {
    }
}
