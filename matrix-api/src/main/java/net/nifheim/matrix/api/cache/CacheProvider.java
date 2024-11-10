package net.nifheim.matrix.api.cache;

import java.util.Optional;
import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.service.MatrixService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service to handle cached data used by Matrix implementations, object instances obtained using this class may be
 * outdated, it is intended for internal usage in implementations and other applications shouldn't rely on this data.
 *
 * @author Jaime Suárez
 */
public interface CacheProvider <T extends MatrixPlayer> extends MatrixService {

    /**
     * Get the UUID associated to an ObjectId from the cache.
     *
     * @param hexId ObjectId to search.
     * @return {@link Optional} representing the result from the cache.
     */
    @NotNull
    Optional<UUID> getUniqueId(String hexId);

    @NotNull
    Optional<String> getHexId(@NotNull UUID uniqueId);

    /**
     * Updates the cache with the given UUID and hexId.
     * If there is already a UUID associated with the hexId in the cache, it is deleted.
     * The new UUID and hexId are then stored in the cache for a specific period of time.
     *
     * @param uniqueId The UUID to update the cache with.
     * @param hexId    The hexId to update the cache with.
     */
    void update(@NotNull UUID uniqueId, @NotNull String hexId);

    @NotNull
    Optional<T> getPlayer(@NotNull String hexId);

    T removePlayer(@NotNull T player);

    boolean isCached(@NotNull String hexId);

    void updateCachedFieldById(@NotNull String hexId, @NotNull String field, @Nullable Object value);

    void add(@NotNull T matrixPlayer);

    void update(@NotNull T matrixPlayer);
}
