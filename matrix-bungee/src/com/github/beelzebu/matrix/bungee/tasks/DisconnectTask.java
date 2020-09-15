package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

/**
 * @author Beelzebu
 */
public class DisconnectTask implements Runnable {

    private final PlayerDisconnectEvent event;
    private final MatrixPlayer player;

    public DisconnectTask(PlayerDisconnectEvent event) {
        this.event = event;
        this.player = Matrix.getAPI().getPlayer(event.getPlayer().getUniqueId());
    }

    @Override
    public void run() {
        try {
            player.setLoggedIn(false);
            if (player.isAdmin() && !event.getPlayer().hasPermission("matrix.admin")) {
                player.setAdmin(false);
            }
            if (player.getLastLogin() != null && player.getRegistration() != null && player.getRegistration().after(player.getLastLogin())) {
                ((MongoMatrixPlayer) player).setRegistration(player.getLastLogin());
            }
            player.setLastLogin(new Date());
            Matrix.getAPI().getPlayers().remove(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
            Matrix.getLogger().debug(e);
        }
    }
}