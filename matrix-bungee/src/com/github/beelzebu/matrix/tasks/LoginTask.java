package com.github.beelzebu.matrix.tasks;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import java.util.Date;
import java.util.Objects;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
public class LoginTask implements Runnable {

    private final MatrixBungeeBootstrap plugin;
    private final LoginEvent event;
    private MatrixPlayer player;
    private boolean firstJoin = false;

    public LoginTask(MatrixBungeeBootstrap plugin, LoginEvent event, MatrixPlayer player) {
        this.plugin = plugin;
        this.event = event;
        this.player = player;
    }

    @Override
    public void run() {
        try {
            PendingConnection pendingConnection = event.getConnection();
            if (player == null) {
                if (pendingConnection.getUniqueId() != null && pendingConnection.getName() != null) {
                    MatrixPlayer playerByName = Matrix.getAPI().getPlayer(pendingConnection.getName());
                    if (playerByName != null) {
                        if (pendingConnection.isOnlineMode()) {
                            player = playerByName;
                        } else if (!Objects.equals(playerByName.getUniqueId(), pendingConnection.getUniqueId())) {
                            event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.UUID_DONTMATCH.getId() + "\n" +
                                    "\n" +
                                    "Your UUID doesn't match with the UUID associated to your name in our database.\n" +
                                    "This login attempt was recorded for security reasons."));
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        player = new MongoMatrixPlayer(pendingConnection.getUniqueId(), pendingConnection.getName()).save();
                        firstJoin = true;
                    }
                } else {
                    event.setCancelled(true);
                    event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.NULL_PLAYER.getId()));
                    return;
                }
            }
            if (!event.getConnection().getName().equalsIgnoreCase("Beelzebu")) {
                if (plugin.getApi().getMaintenanceManager().isMaintenance() && !player.isAdmin()) {
                    event.setCancelled(true);
                    event.setCancelReason(TextComponent.fromLegacyText(I18n.tl(Message.MAINTENANCE, player.getLastLocale())));
                    return;
                }
            }
            if (pendingConnection.getUniqueId() != null && pendingConnection.getName() != null) {
                if (player.getUniqueId() == null || player.getUniqueId() != pendingConnection.getUniqueId()) {
                    player.setUniqueId(pendingConnection.getUniqueId());
                }
                player.setName(pendingConnection.getName());
                if (pendingConnection.isOnlineMode()) {
                    player.setPremium(true);
                    player.setRegistered(true);
                    player.setLoggedIn(true);
                }
                if (!Matrix.getAPI().getCache().isCached(player.getUniqueId())) {
                    plugin.getApi().getCache().saveToCache(player);
                }
                if (firstJoin) {
                    player.setOption(PlayerOptionType.SPEED, true);
                }
                player.setLastLogin(new Date());
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getLocalizedMessage()));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(plugin);
        }
    }
}
