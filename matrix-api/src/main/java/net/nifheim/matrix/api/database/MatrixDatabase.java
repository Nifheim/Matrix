package net.nifheim.matrix.api.database;

import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.service.MatrixService;
import org.jetbrains.annotations.Nullable;

/**
 * Service to handle persistent data used by Matrix implementations, object instances obtained using this class may be
 * outdated, it is intended for internal usage in implementations and other applications shouldn't rely on this data.
 *
 * @author Jaime Suárez
 */
public interface MatrixDatabase extends MatrixService {

    /**
     * Get a {@link MatrixPlayer} from the database using his minecraft unique id, it works both for cracked (uuid v3) and premium (uuid v4) players.
     *
     * @param uniqueId minecraft unique id to check.
     * @return {@link MatrixPlayer} request to the database.
     * @see #getPlayer(String) get a player by his ObjectId.
     */
    @Nullable MatrixPlayer getPlayer(UUID uniqueId);

    /**
     * Get a {@link MatrixPlayer} from the database using his internal ObjectId, it works both for cracked (uuid v3) and premium (uuid v4) players.
     *
     * @param hexId ObjectId to check.
     * @return {@link MatrixPlayer} request to the database.
     * @see #getPlayer(UUID) get a player by his unique id.
     */
    @Nullable MatrixPlayer getPlayer(String hexId);

    /**
     * Check if a {@link MatrixPlayer} is stored.
     *
     * @param matrixPlayer {@link MatrixPlayer} instance to check.
     * @return <i>true</i> if the player is stored, <i>false</i> otherwise.
     * @see #isStored(UUID) check if player is stored using his {@link UUID}.
     * @see #isStored(String) check if player is stored using his minecraft username.
     */
    boolean isStored(MatrixPlayer matrixPlayer);

    /**
     * Check if a {@link MatrixPlayer} is stored with his minecraft {@link UUID}, it can be used when only the
     * {@link UUID} is known and there is no available {@link MatrixPlayer} instance or the cost of obtaining
     * it over the {@link UUID} is higher.
     *
     * @param uniqueId minecraft {@link UUID} to check, it can be a premium or cracked account.
     * @return <i>true</i> if the player is stored, <i>false</i> otherwise.
     * @see #isStored(MatrixPlayer) check if player is stored using his {@link MatrixPlayer} instance.
     * @see #isStored(String) check if player is stored using his ObjectId.
     */
    boolean isStored(UUID uniqueId);

    /**
     * Check if a {@link MatrixPlayer} is stored with his internal ObjectId, it can be used when only the ObjectId
     * is known and there is no available {@link MatrixPlayer} instance or the cost of obtaining it over the
     * ObjectId is higher.
     *
     * @param hexId ObjectId to check.
     * @return <i>true</i> if the player is stored, <i>false</i> otherwise.
     * @see #isStored(MatrixPlayer) check if player is stored using his {@link MatrixPlayer} instance.
     * @see #isStored(UUID) check if player is stored using his minecraft {@link UUID}.
     */
    boolean isStored(String hexId);
}
