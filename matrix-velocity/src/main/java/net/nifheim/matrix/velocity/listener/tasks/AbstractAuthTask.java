package net.nifheim.matrix.velocity.listener.tasks;

import com.velocitypowered.api.proxy.Player;
import java.util.UUID;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractAuthTask {

    protected final Logger logger;
    protected final PlayerManagerImpl<Player> playerManager;

    protected AbstractAuthTask(Logger logger, PlayerManagerImpl<Player> playerManager) {
        this.logger = logger;
        this.playerManager = playerManager;
    }

    /**
     * Get the player from the database, try first with the hex if it exits, then try uniqueId, if there is no player with that uniqueId, then the player does not exist.
     *
     * @param uniqueId the uniqueId of the player
     * @param name     the name of the player
     * @return the player from the database
     */
    protected final @Nullable MongoMatrixPlayer getPlayer(UUID uniqueId, String name) {
        MongoMatrixPlayer player = null;
        String hexId = playerManager.getHexId(uniqueId);
        if (hexId != null) {
            player = playerManager.getPlayerByIdSync(hexId);
        }
        if (player == null) {
            logger.warn("Player not found by hexId for {} ({})", name, uniqueId);
            player = playerManager.getPlayerByUniqueIdSync(uniqueId);
            if (player == null) {
                logger.warn("Player not found by uniqueId for {} ({})", name, uniqueId);
            }
        }
        return player;
    }

}
