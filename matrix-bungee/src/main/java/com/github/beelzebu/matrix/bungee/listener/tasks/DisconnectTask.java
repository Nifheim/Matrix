package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

/**
 * @author Beelzebu
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