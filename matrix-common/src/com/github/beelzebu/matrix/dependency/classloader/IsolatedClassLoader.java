package com.github.beelzebu.matrix.dependency.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Beelzebu
 */
public class IsolatedClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public IsolatedClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
}
