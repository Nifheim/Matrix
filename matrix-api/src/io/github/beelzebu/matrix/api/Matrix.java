package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Beelzebu
 */
public final class Matrix {

    public static final Gson GSON = new Gson();

    @Getter
    @Setter
    private static MatrixAPI API = null;

    public static Optional<MatrixAPI> getAPISafe() {
        return Optional.ofNullable(API);
    }
}
