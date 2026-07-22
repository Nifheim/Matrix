package net.nifheim.matrix.api.cache;

import java.util.Optional;
import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.service.MatrixService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service to handle cached data used by Matrix implementations, object instances obtained using this class may be
 * outdated, it is intended for internal usage in implementations, and other applications shouldn't rely on this data.
 *
 * @author Jaime Suárez
 */
public interface CacheProvider <T extends MatrixPlayer> extends MatrixService {

    @NotNull Optional<T> getPlayer(@NotNull UUID uniqueId);

    T removePlayer(@NotNull T player);

    boolean isCached(UUID uniqueId);

    void updateCachedFieldById(@NotNull UUID uniqueId, @NotNull String field, @Nullable Object value);

    void add(@NotNull T player);

    void update(@NotNull T player);
}
