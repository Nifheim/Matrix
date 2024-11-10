package net.nifheim.matrix.velocity.listener.tasks;


import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Date;
import net.kyori.adventure.text.Component;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.scheduler.Throwing;
import net.nifheim.matrix.common.util.ErrorCodes;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class DisconnectTask extends AbstractAuthTask implements Throwing.Runnable {

    private final DisconnectEvent event;

    public DisconnectTask(Logger logger, DisconnectEvent event, PlayerManagerImpl<Player> playerManager) {
        super(logger, playerManager);
        this.event = event;
    }


    @Override
    public void run() {
        final MongoMatrixPlayer player = getPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
        if (player != null) {
            logger.debug("Processing disconnect for matrix player {} ({})", player.getName(), player.getId());
            try {
                player.setLoggedIn(false);
                player.setLastLogin(new Date());
                playerManager.disconnect(player);
            } catch (Exception e) {
                event.getPlayer().disconnect(Component.text("There was a problem processing your disconnect, error code: " + ErrorCodes.UNKNOWN.getId()));
                logger.warn("There was a problem processing disconnect for " + event.getPlayer().getUsername() + " (" + event.getPlayer().getUniqueId() + ")", e);
            }
        } else {
            logger.warn("Skipping disconnection for null matrix player: {} ({})", event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
        }
    }
}
