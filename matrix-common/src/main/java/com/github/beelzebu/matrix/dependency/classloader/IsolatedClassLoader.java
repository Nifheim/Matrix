package com.github.beelzebu.matrix.dependency.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class IsolatedClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public IsolatedClassLoader(URL @NotNull [] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
}
