package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.api.player.StatsAdapter;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Beelzebu
 */
public final class Matrix {

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Statistics.class, new StatsAdapter()).create();

    @Getter
    @Setter
    private static MatrixAPI API = null;

    public static Optional<MatrixAPI> getAPISafe() {
        return Optional.ofNullable(API);
    }
}
