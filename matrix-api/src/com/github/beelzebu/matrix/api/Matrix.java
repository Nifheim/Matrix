package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.logger.MatrixLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
@SuppressWarnings("deprecation")
public final class Matrix {

    public static final String IP = "mc.indiopikaro.cl";
    public static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapter(ChatColor.class, new TypeAdapter<ChatColor>() {
        @Override
        public void write(JsonWriter out, ChatColor value) throws IOException {
            out.value(value.name());
        }

        @Override
        public ChatColor read(JsonReader in) throws IOException {
            return ChatColor.valueOf(in.nextString());
        }
    }).create();
    private static MatrixLogger LOGGER;
    private static MatrixAPI API;

    public static MatrixLogger getLogger() {
        return LOGGER;
    }

    public static MatrixAPI getAPI() {
        return Matrix.API;
    }

    public static void setAPI(MatrixAPI api) {
        if (API != null) {
            throw new RuntimeException("API is already defined.");
        }
        API = api;
    }

    public static void setLogger(MatrixLogger logger) {
        if (LOGGER != null) {
            throw new RuntimeException("LOGGER is already defined.");
        }
        LOGGER = logger;
    }
}
