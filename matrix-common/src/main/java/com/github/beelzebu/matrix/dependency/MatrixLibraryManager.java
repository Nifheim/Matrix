package com.github.beelzebu.matrix.dependency;

import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.URLClassLoaderHelper;
import net.byteflux.libby.logging.adapters.JDKLogAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixLibraryManager extends LibraryManager {

    private final URLClassLoaderHelper classLoader;

    public MatrixLibraryManager(@NotNull MatrixBootstrap plugin) {
        super(new JDKLogAdapter(Objects.requireNonNull(plugin, "plugin").getLogger()), plugin.getDataFolder().toPath());
        classLoader = new URLClassLoaderHelper((URLClassLoader) plugin.getClass().getClassLoader());
    }

    @Override
    protected void addToClasspath(Path file) {
        classLoader.addToClasspath(file);
    }
}
