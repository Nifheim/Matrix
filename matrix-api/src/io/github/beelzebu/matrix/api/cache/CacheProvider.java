package io.github.beelzebu.matrix.api.cache;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public interface CacheProvider {

    /**
     * Get the UUID associated to a username from the cache.
     *
     * @param name username to search.
     * @return {@link Optional} representing the result from the cache.
     */
    Optional<UUID> getUniqueId(String name);

    /**
     * Get the Name associated to a UUID from the cache.
     *
     * @param uniqueId UUID to search
     * @return {@link Optional} representing the result from the cache.
     */
    Optional<String> getName(UUID uniqueId);

    void update(String name, UUID uniqueId);

    Optional<MatrixPlayer> getPlayer(UUID uniqueId);

    Optional<MatrixPlayer> getPlayer(String name);

    Set<MatrixPlayer> getPlayers();

    void removePlayer(MatrixPlayer player);
}
