package io.github.beelzebu.matrix.api;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Beelzebu
 */
public final class Matrix {

    @Getter
    @Setter
    private static MatrixAPI API = null;

    public static Optional<MatrixAPI> getAPISafe() {
        return Optional.ofNullable(API);
    }
}
