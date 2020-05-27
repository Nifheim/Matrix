package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import io.github.beelzebu.matrix.api.logging.MatrixLogger;
import java.util.Optional;

/**
 * @author Beelzebu
 */
public final class Matrix {

    public static final Gson GSON = new Gson();
    private static final MatrixLogger LOGGER = new MatrixLogger();

    private static MatrixAPI API = null;

    public static Optional<MatrixAPI> getAPISafe() {
        return Optional.ofNullable(API);
    }

    public static MatrixLogger getLogger() {
        return LOGGER;
    }

    public static MatrixAPI getAPI() {
        return Matrix.API;
    }

    public static void setAPI(MatrixAPI API) {
        Matrix.API = API;
    }
}
