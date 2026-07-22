package net.nifheim.matrix.velocity.listener.tasks;

import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractAuthTask {

    protected final Logger logger;
    protected final PlayerManager playerManager;

    protected AbstractAuthTask(Logger logger, PlayerManager playerManager) {
        this.logger = logger;
        this.playerManager = playerManager;
    }

    /**
     * Get the player from the database, try first with the hex if it exits, then try uniqueId, if there is no player
     * with that uniqueId, then the player does not exist.
     *
     * @param uniqueId the uniqueId of the player
     * @param name     the name of the player
     * @return the player from the database
     */
    public final @Nullable MatrixPlayer getPlayer(UUID uniqueId, String name) {
        MatrixPlayer player = playerManager.getPlayerSync(uniqueId);
        if (player == null) {
            logger.warn("Player not found by UUID for {} ({})", name, uniqueId);
            player = playerManager.getPlayerSync(name);
            if (player == null) {
                logger.warn("Player not found by name for {} ({})", name, uniqueId);
            }
        }
        return player;
    }

}
