package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

/**
 * @author Beelzebu
 */
public class DisconnectTask implements Runnable {

    private final MatrixBungeeAPI api;
    private final PlayerDisconnectEvent event;
    private final MatrixPlayer player;

    public DisconnectTask(MatrixBungeeAPI api, PlayerDisconnectEvent event) {
        this.api = api;
        this.event = event;
        this.player = api.getPlayerManager().getPlayer(event.getPlayer()).join();
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
            api.getDatabase().cleanUp(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
            Matrix.getLogger().debug(e);
        }
    }
}