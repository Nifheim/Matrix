package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.exception.LoginException;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
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
            String name = Objects.requireNonNull(event.getConnection().getName(), "name");
            UUID uniqueId = Objects.requireNonNull(event.getConnection().getUniqueId(), "uniqueId");
            Matrix.getLogger().debug("Processing login for " + name);
            MongoMatrixPlayer player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(name).join();
            if (player == null) { // new player, so we need to create it
                player = new MongoMatrixPlayer(uniqueId, name);
                player.save().join(); // block until player is saved
                player.setLastLocale("es");
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
