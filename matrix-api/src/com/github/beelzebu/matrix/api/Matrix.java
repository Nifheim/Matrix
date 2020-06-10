package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.logger.MatrixLogger;
import com.google.gson.Gson;

/**
 * @author Beelzebu
 */
public final class Matrix {

    public static final String IP = "mc.indiopikaro.cl";
    public static final Gson GSON = new Gson();
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
