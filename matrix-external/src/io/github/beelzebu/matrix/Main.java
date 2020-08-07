package com.github.beelzebu.matrix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.plugin.MatrixExternalPlugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * @author Beelzebu
 */
public class Main {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;
    private HashMap<String, Object> config;

    {
        try {
            configFile = new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "config.json");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't load configFile", e);
        }
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

    public Gson getGson() {
        return gson;
    }

    public File getConfigFile() {
        return configFile;
    }

    public HashMap<String, Object> getConfig() {
        return config;
    }

    private void run(String[] args) {
        if (!configFile.exists()) {
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("config.json"), configFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Can't copy configFile", e);
            }
        }
        try {
            config = gson.fromJson(new FileReader(configFile), HashMap.class);
        } catch (FileNotFoundException | JsonSyntaxException e) {
            throw new RuntimeException("Can't load config from JSON", e);
        }
        Matrix.setAPI(new MatrixAPIImpl(new MatrixExternalPlugin(this)));
    }
}
