package com.github.beelzebu.matrix.dependency.classloader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class ReflectionClassLoader {

    private static final @NotNull Method ADD_URL_METHOD;

    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NotNull URLClassLoader classLoader;

    public ReflectionClassLoader(@NotNull Object plugin) throws IllegalStateException {
        ClassLoader clazzLoader = plugin.getClass().getClassLoader();
        if (clazzLoader instanceof URLClassLoader) {
            classLoader = (URLClassLoader) clazzLoader;
        } else {
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        }
    }

    public void loadJar(URL url) {
        try {
            ADD_URL_METHOD.invoke(classLoader, url);
        } catch (@NotNull IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadJar(@NotNull Path file) {
        try {
            loadJar(file.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

