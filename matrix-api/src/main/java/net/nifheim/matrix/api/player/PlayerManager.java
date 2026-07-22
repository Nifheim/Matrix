package net.nifheim.matrix.api.player;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.nifheim.matrix.api.cache.CacheProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * A manager that provides methods to interact with the players in the network, it can be used to get information about
 * the players, such as their current server, group, or if they are online.
 * </p>
 * <p>
 * It can also be used to set the server, group, or online status of a player, it can be used to get the player by their
 * {@link UUID} or their ObjectId.
 * </p>
 *
 * @author Jaime Suárez
 * @see MatrixPlayer
 */
public interface PlayerManager {

    /**
     * Get the player by their {@link UUID}.
     *
     * @param uniqueId {@link UUID} of the player to get.
     * @return {@link CompletableFuture} that will complete with the player if found, or null if not found.
     */
    @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayer(UUID uniqueId);

    @Nullable MatrixPlayer getPlayerSync(@NotNull UUID uniqueId);

    @Nullable MatrixPlayer getPlayerSync(@NotNull String name);

    @NotNull MatrixPlayer createPlayer(@NotNull UUID uniqueId, @NotNull String name, @Nullable Locale locale);

    /**
     * Get cache provider that this player manager is using.
     *
     * @return The cache provider that this player manager is using.
     */
    @NotNull CacheProvider<MatrixPlayer> getCacheProvider();

    /**
     * Update a cached property of a player on the cache and propagates the information to other servers that may hold a
     * copy of this player instance.
     *
     * @param matrixPlayer player to update.
     * @param property     field to update.
     * @param value        value to set.
     * @throws ReflectiveOperationException if the field could not be updated.
     */
    void updateProperty(MatrixPlayer matrixPlayer, String property, Object value) throws ReflectiveOperationException;

    /**
     * Save the player to the database. Updating it on the cache, sending the update to other servers where the player
     * information
     * might be cached and storing it in the database.
     *
     * @param player Player to save.
     * @return A {@link CompletableFuture} that will complete when the player is saved.
     */
    @NotNull CompletableFuture<Void> save(MatrixPlayer player);
}
