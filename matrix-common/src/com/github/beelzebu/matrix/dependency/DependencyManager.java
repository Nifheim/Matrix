/*
 * This file is part of coins3
 *
 * Copyright © 2020 Beelzebu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.beelzebu.matrix.dependency;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.dependency.classloader.IsolatedClassLoader;
import com.github.beelzebu.matrix.dependency.classloader.ReflectionClassLoader;
import com.github.beelzebu.matrix.dependency.relodaction.Relocation;
import com.github.beelzebu.matrix.dependency.relodaction.RelocationHandler;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Beelzebu
 */
public final class DependencyManager {

    private final MatrixPlugin plugin;
    private final ReflectionClassLoader reflectionClassLoader;
    private final DependencyRegistry registry;
    private final Map<Dependency, Path> loaded = new EnumMap<>(Dependency.class);
    private final Map<ImmutableSet<Dependency>, IsolatedClassLoader> loaders = new HashMap<>();
    private RelocationHandler relocationHandler;

    public DependencyManager(MatrixPlugin plugin, ReflectionClassLoader reflectionClassLoader, DependencyRegistry registry) {
        this.plugin = plugin;
        this.reflectionClassLoader = reflectionClassLoader;
        this.registry = registry;
    }

    public void loadInternalDependencies() {
        Set<Dependency> dependencies = EnumSet.of(Dependency.CAFFEINE,
                Dependency.HIKARI,
                Dependency.MARIADB_DRIVER,
                Dependency.COMMONS_POOL_2,
                Dependency.JEDIS
        );
        loadDependencies(dependencies);
    }

    public void loadDependencies(Set<Dependency> dependencies) {
        Path saveDirectory = getSaveDirectory();
        List<Source> sources = new ArrayList<>();
        dependencies.stream().filter(dependency -> !loaded.containsKey(dependency)).forEachOrdered(dependency -> {
            try {
                Path file = downloadDependency(saveDirectory, dependency);
                sources.add(new Source(dependency, file));
            } catch (Exception ex) {
                Matrix.getLogger().info("Exception whilst downloading dependency " + dependency.name());
                Matrix.getLogger().debug(ex);
            }
        });
        List<Source> remappedJars = new ArrayList<>(sources.size());
        for (Source source : sources) {
            try {
                List<Relocation> relocations = source.getDependency().getRelocations();
                if (relocations.isEmpty()) {
                    remappedJars.add(source);
                    continue;
                }
                Path input = source.getFile();
                Path output = input.getParent().resolve("remapped-" + input.getFileName().toString());
                if (Files.exists(output)) {
                    remappedJars.add(new Source(source.getDependency(), output));
                    continue;
                }
                Matrix.getLogger().info("Attempting to apply relocations to " + input.getFileName().toString() + "...");
                relocationHandler = new RelocationHandler(this);
                relocationHandler.remap(input, output, relocations);
                remappedJars.add(new Source(source.getDependency(), output));
            } catch (Exception ex) {
                Matrix.getLogger().info("Unable to remap the source file '" + source.getDependency().name() + "'.");
                Matrix.getLogger().debug(ex);
            }
        }
        remappedJars.forEach(jar -> {
            if (!registry.shouldAutoLoad(jar.getDependency())) {
                loaded.put(jar.getDependency(), jar.getFile());
            } else {
                try {
                    reflectionClassLoader.loadJar(jar.getFile());
                    loaded.put(jar.getDependency(), jar.getFile());
                } catch (Throwable ex) {
                    Matrix.getLogger().info("Failed to load dependency jar '" + jar.getFile().getFileName().toString() + "'.");
                    Matrix.getLogger().debug(ex.getMessage());
                }
            }
        });
    }

    public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
        ImmutableSet<Dependency> set = ImmutableSet.copyOf(dependencies);
        dependencies.stream().filter(dependency -> !loaded.containsKey(dependency)).forEachOrdered(dependency -> {
            throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
        });
        synchronized (loaders) {
            IsolatedClassLoader classLoader = loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }
            URL[] urls = set.stream().map(loaded::get).map(file -> {
                try {
                    return file.toUri().toURL();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }).toArray(URL[]::new);
            classLoader = new IsolatedClassLoader(urls);
            loaders.put(set, classLoader);
            return classLoader;
        }
    }


    private Path getSaveDirectory() {
        Path saveDirectory = new File(plugin.getDataFolder(), "lib").toPath();
        try {
            Files.createDirectories(saveDirectory);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create lib directory", ex);
        }
        return saveDirectory;
    }


    private Path downloadDependency(Path saveDirectory, Dependency dependency) throws Exception {
        String fileName = dependency.name().toLowerCase() + "-" + dependency.getVersion() + ".jar";
        Path file = saveDirectory.resolve(fileName);
        if (Files.exists(file)) {
            return file;
        }
        URL url = new URL(dependency.getUrl());
        try (InputStream in = url.openStream()) {
            byte[] bytes = ByteStreams.toByteArray(in);
            if (bytes.length == 0) {
                throw new RuntimeException("Empty stream");
            }
            Matrix.getLogger().info("Successfully downloaded '" + fileName + "'");
            Files.write(file, bytes);
        }
        if (!Files.exists(file)) {
            throw new IllegalStateException("File not present. - " + file.toString());
        } else {
            return file;
        }
    }

    private static final class Source {

        private final Dependency dependency;
        private final Path file;

        private Source(Dependency dependency, Path file) {
            this.dependency = dependency;
            this.file = file;
        }

        public Dependency getDependency() {
            return dependency;
        }

        public Path getFile() {
            return file;
        }
    }
}
