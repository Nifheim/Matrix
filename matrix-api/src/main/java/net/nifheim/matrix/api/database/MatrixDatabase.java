package net.nifheim.matrix.api.database;

import java.net.InetAddress;
import java.util.Locale;
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
     * Get a {@link MatrixPlayer} from the database using his minecraft unique id, it works both for cracked (uuid v3)
     * and premium (uuid v4) players.
     *
     * @param uniqueId minecraft unique id to check.
     * @return {@link MatrixPlayer} request to the database.
     */
    @Nullable MatrixPlayer getPlayer(UUID uniqueId);

    MatrixPlayer createPlayer(UUID uniqueId, String name, Locale locale);

    MatrixPlayer save(MatrixPlayer player);

    long saveHandshakeRequest(InetAddress address, int protocol, String version, @Nullable String hostname);

    /**
     * Check if a {@link MatrixPlayer} is stored with his minecraft {@link UUID}, it can be used when only the
     * {@link UUID} is known and there is no available {@link MatrixPlayer} instance or the cost of obtaining
     * it over the {@link UUID} is higher.
     *
     * @param uniqueId minecraft {@link UUID} to check, it can be a premium or cracked account.
     * @return <i>true</i> if the player is stored, <i>false</i> otherwise.
     */
    boolean isStored(UUID uniqueId);
}
