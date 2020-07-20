package com.github.beelzebu.matrix.api.cache;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Map;
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

    boolean isCached(UUID uniqueId);

    Set<String> getGroups();

    Map<String, Set<String>> getAllServers();

    Set<String> getServers(String group);

    boolean isGroupRegistered(String group);

    void registerGroup(String name);

    void addServer(String group, String server);

    void addServer(String group, String[] servers);

    void removeServer(String name);

    void updateCachedField(MatrixPlayer matrixPlayer, String field, Object value);

    MatrixPlayer saveToCache(MatrixPlayer matrixPlayer);

    void setDiscordVerificationCode(String name, String code);

    void purgeForAllPlayers(String field);

    void shutdown();
}
