package net.nifheim.matrix.bungee.listener.tasks;

import net.nifheim.matrix.api.Matrix;
import net.nifheim.matrix.api.MatrixBungeeAPI;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.util.Throwing;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

/**
 * @author Jaime Suárez
 */
public class DisconnectTask implements Throwing.Runnable {

    private final MatrixBungeeAPI api;
    private final PlayerDisconnectEvent event;

    public DisconnectTask(MatrixBungeeAPI api, PlayerDisconnectEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        MatrixPlayer player = api.getPlayerManager().getPlayer(event.getPlayer()).join();
        if (player != null) {
            Matrix.getLogger().debug("Processing disconnect for matrix player " + player.getName() + " " + player.getId());
            try {
                player.setLoggedIn(false);
                if (player.getLastLogin() != null) {
                    if (player.getRegistration().after(player.getLastLogin())) {
                        ((MongoMatrixPlayer) player).setRegistration(player.getLastLogin());
                    }
                }
                player.setLastLogin(new Date());
                api.getDatabase().cleanUp(player);
            } catch (Exception e) {
                event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
                Matrix.getLogger().debug(e);
            }
        } else {
            Matrix.getLogger().debug("Skipping disconnection for null matrix player: " + event.getPlayer().getName() + " " + event.getPlayer().getUniqueId());
        }
    }
}
