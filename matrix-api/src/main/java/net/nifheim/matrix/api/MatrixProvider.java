package net.nifheim.matrix.api;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public final class MatrixProvider {

    private static MatrixAPI API;

    /**
     * Get current {@link MatrixAPI} instance.
     *
     * @return
     */
    public static MatrixAPI getAPI() {
        if (API == null) {
            throw new RuntimeException("API instance is not available.");
        }
        return MatrixProvider.API;
    }

    public static void setAPI(@NotNull MatrixAPI api) {
        Objects.requireNonNull(api, "api");
        if (API != null) {
            throw new RuntimeException("API is already defined.");
        }
        API = api;
    }
}
