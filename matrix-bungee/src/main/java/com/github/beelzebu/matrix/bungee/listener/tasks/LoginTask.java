package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
public class LoginTask implements Runnable {

    private final MatrixBungeeAPI api;
    private final LoginEvent event;

    public LoginTask(MatrixBungeeAPI api, LoginEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        try {
            MongoMatrixPlayer player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(event.getConnection().getUniqueId()).join();
            if (player == null) {
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(event.getConnection().getName()).join();
                if (event.getConnection().getUniqueId().version() == 4) {
                    player.setPremium(true);
                    player.setUniqueId(event.getConnection().getUniqueId());
                    player.save().join();
                }
            }
            if (player == null) {
                Matrix.getLogger().info("Player null login name: " + event.getConnection().getName() + " uuid: " + event.getConnection().getUniqueId());
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.NULL_PLAYER.getId()));
                return;
            }
            PendingConnection pendingConnection = event.getConnection();
            if (!event.getConnection().getName().equalsIgnoreCase("Beelzebu") && api.getMaintenanceManager().isMaintenance() && !player.isAdmin()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(I18n.tl(Message.MAINTENANCE, player.getLastLocale())));
                return;
            }
            if (pendingConnection.getUniqueId().version() == 4 && player.getUniqueId() != pendingConnection.getUniqueId()) {
                player.setUniqueId(pendingConnection.getUniqueId());
            }
            player.setName(pendingConnection.getName());
            if (pendingConnection.isOnlineMode() || player.isBedrock()) {
                player.setPremium(true);
                player.setRegistered(true);
                player.setLoggedIn(true);
            }
            player.setLastLogin(new Date());
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId()));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }
}
