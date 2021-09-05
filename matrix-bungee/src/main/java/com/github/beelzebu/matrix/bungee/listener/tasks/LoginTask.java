package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.exception.LoginException;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.LoginState;
import java.util.Date;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
public class LoginTask implements Throwing.Runnable {

    private final MatrixBungeeAPI api;
    private final LoginEvent event;

    public LoginTask(MatrixBungeeAPI api, LoginEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        try {
            Matrix.getLogger().debug("Processing login for " + event.getConnection().getName());
            MongoMatrixPlayer player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(event.getConnection().getUniqueId()).join();
            if (player == null) { // this shouldn't happen
                Matrix.getLogger().info("Player by uuid is null for " + event.getConnection().getName());
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(event.getConnection().getName()).join();
                if (event.getConnection().getUniqueId().version() == 4) {
                    Matrix.getLogger().info("UUID for the connection seems to be premium, forcing premium for " + player.getName() + " " + player.getId());
                    player.setPremium(true);
                    player.setUniqueId(event.getConnection().getUniqueId());
                    player.save().join();
                }
            }
            if (player == null) {
                throw new LoginException(ErrorCodes.NULL_PLAYER, LoginState.LOGIN);
            }
            Matrix.getLogger().debug("Login started for " + player.getName() + " " + player.getId());
            PendingConnection pendingConnection = event.getConnection();
            if (api.getMaintenanceManager().isMaintenance()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(I18n.tl(Message.MAINTENANCE, player.getLastLocale())));
                return;
            }
            if (pendingConnection.getUniqueId().version() == 4 && player.getUniqueId() != pendingConnection.getUniqueId()) {
                player.setUniqueId(pendingConnection.getUniqueId());
            }
            if (pendingConnection.isOnlineMode()) {
                player.setPremium(true);
                player.setRegistered(true);
            }
            player.setLastLogin(new Date());
        } catch (Exception e) {
            if (e instanceof LoginException) {
                event.setCancelReason(new TextComponent("There was a problem processing your login, error code: " + ((LoginException) e).getErrorCodes().getId()));
            } else {
                event.setCancelReason(new TextComponent("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId()));
            }
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }
}
