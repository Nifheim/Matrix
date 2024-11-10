package net.nifheim.matrix.api.player;

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
     * Get the player by their ObjectId.
     *
     * @param hexId ObjectId of the player to get.
     * @return {@link CompletableFuture} that will complete with the player if found, or null if not found.
     */
    @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayerById(String hexId);

    /**
     * Get the player by their {@link UUID}.
     *
     * @param uniqueId {@link UUID} of the player to get.
     * @return {@link CompletableFuture} that will complete with the player if found, or null if not found.
     */
    @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayerByUniqueId(UUID uniqueId);

    /**
     * Get the ObjectId of the player by their {@link UUID}.
     *
     * @param uniqueId {@link UUID} of the player to get the ObjectId of.
     * @return {@link CompletableFuture} that will complete with the ObjectId of the player if found, or null if not found.
     */
    //@NotNull CompletableFuture<@Nullable String> getHexIdByUniqueId(UUID uniqueId);

    /**
     * Get the {@link UUID} of the player by their ObjectId.
     *
     * @param hexId ObjectId of the player to get the {@link UUID} of.
     * @return {@link CompletableFuture} that will complete with the {@link UUID} of the player if found, or null if not found.
     */
    //@NotNull CompletableFuture<@Nullable UUID> getUniqueIdById(String hexId);

    /**
     * Remove a player from the cache.
     * <p>
     * This method does the following:
     * <ol>
     *     <li>Remove the player from the cache.</li>
     *     <li>Store the last cached data in the database.</li>
     * </ol>
     * This method should be used when the player logs out or leaves the network.
     * </p>
     */
    void disconnect(@NotNull MatrixPlayer player);

    /**
     * Get cache provider that this player manager is using.
     *
     * @return The cache provider that this player manager is using.
     */
    @NotNull CacheProvider<? extends MatrixPlayer> getCacheProvider();

    /**
     * Update a cached property of a player on the cache and propagates the information to other servers that may hold a copy of this player instance.
     *
     * @param matrixPlayer player to update.
     * @param property     field to update.
     * @param value        value to set.
     * @throws ReflectiveOperationException if the field could not be updated.
     */
    void updateProperty(MatrixPlayer matrixPlayer, String property, Object value) throws ReflectiveOperationException;

    /**
     * Propagates the update of a MatrixPlayer to other servers that may hold a copy of this player instance.
     * This method will update a cached property of the player in the cache and send the update to other servers.
     *
     * @param player the MatrixPlayer object to update
     */
    void propagateUpdate(MatrixPlayer player);

    /**
     * Save the player to the database. Updating it on the cache, sending the update to other servers where the player information
     * might be cached and storing it in the database.
     *
     * @param player Player to save.
     * @return A {@link CompletableFuture} that will complete when the player is saved.
     */
    @NotNull CompletableFuture<Void> save(MatrixPlayer player);
}
