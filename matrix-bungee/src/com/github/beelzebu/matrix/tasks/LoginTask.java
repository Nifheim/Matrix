package com.github.beelzebu.matrix.tasks;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.util.StringUtils;
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

    public LoginTask(MatrixBungeeBootstrap plugin, LoginEvent event, MatrixPlayer player) {
        this.plugin = plugin;
        this.event = event;
        this.player = player;
    }

    @Override
    public void run() {
        try {
            PendingConnection pc = event.getConnection();
            if (player == null) {
                if (pc.getUniqueId() != null && pc.getName() != null) {
                    MatrixPlayer playerByName = Matrix.getAPI().getPlayer(pc.getName());
                    if (playerByName != null) {
                        if (pc.isOnlineMode()) {
                            playerByName.setUniqueId(pc.getUniqueId());
                            player = playerByName;
                        } else if (!Objects.equals(playerByName.getUniqueId(), pc.getUniqueId())) {
                            event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.UUID_DONTMATCH.getId() + "\n" +
                                    "\n" +
                                    "Your UUID doesn't match with the UUID associated to your name in our database.\n" +
                                    "This login attempt was recorded for security reasons."));
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        player = new MongoMatrixPlayer(pc.getUniqueId(), pc.getName()).save();
                        player.setOption(PlayerOptionType.SPEED, true);
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
                    event.setCancelReason(TextComponent.fromLegacyText(StringUtils.replace(Message.MAINTENANCE.getDefaults())));
                    return;
                }
            }
            if (pc.getUniqueId() != null && pc.getName() != null) {
                player.setUniqueId(pc.getUniqueId());
                player.setName(pc.getName());
                if (pc.isOnlineMode()) {
                    player.setPremium(true);
                    player.setRegistered(true);
                    player.setLoggedIn(true);
                }
                player.setLastLogin(new Date());
                if (!Matrix.getAPI().getCache().isCached(player.getUniqueId())) {
                    plugin.getApi().getCache().saveToCache(player);
                }
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
