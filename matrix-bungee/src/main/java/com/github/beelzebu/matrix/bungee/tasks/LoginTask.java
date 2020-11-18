package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.RateLimitException;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import org.geysermc.floodgate.FloodgateAPI;

/**
 * @author Beelzebu
 */
public class LoginTask implements IndioLoginTask {

    private final MatrixBungeeBootstrap plugin;
    private final LoginEvent event;
    private MongoMatrixPlayer player;
    private boolean firstJoin = false;

    public LoginTask(MatrixBungeeBootstrap plugin, LoginEvent event) {
        this.plugin = plugin;
        this.event = event;
        MatrixPlayer player = Matrix.getAPI().getPlayer(event.getConnection().getUniqueId());
        if (player == null) {
            Matrix.getLogger().info("Player null login name: " + event.getConnection().getName() + " uuid: " + event.getConnection().getUniqueId());
            if (event.getConnection().getName() != null) {
                Profile profile = null;
                try {
                    profile = RESOLVER.findProfile(event.getConnection().getName()).orElse(null);
                } catch (RateLimitException | IOException e) {
                    e.printStackTrace();
                }
                if (profile != null) {
                    player = Matrix.getAPI().getPlayer(profile.getId());
                    if (player != null) {
                        if (event.getConnection().getName() != null && !Objects.equals(player.getName(), event.getConnection().getName())) {
                            player.setName(event.getConnection().getName());
                        }
                    }
                }
                if (player == null) {
                    player = Matrix.getAPI().getPlayer(event.getConnection().getName());
                }
            }
        }
        this.player = (MongoMatrixPlayer) player;
    }

    @Override
    public void run() {
        try {
            PendingConnection pendingConnection = event.getConnection();
            if (player == null) {
                player = new MongoMatrixPlayer(pendingConnection.getUniqueId(), pendingConnection.getName()).save();
                firstJoin = true;
            }
            if (FloodgateAPI.isBedrockPlayer(player.getUniqueId())) {
                player.setBedrock(true);
            }
            if (!player.isPremium() && !Objects.equals(player.getUniqueId(), UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getConnection().getName()).getBytes()))) {
                event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.UUID_DONTMATCH.getId() + "\n\nYour UUID doesn't match with the UUID associated to your name in our database.\nThis login attempt was recorded for security reasons."));
                event.setCancelled(true);
                Matrix.getAPI().getDatabase().addFailedLogin(event.getConnection().getUniqueId(), event.getConnection().getName(), "error login bungee");
                return;
            }
            if (!event.getConnection().getName().equalsIgnoreCase("Beelzebu") && plugin.getApi().getMaintenanceManager().isMaintenance() && !player.isAdmin()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(I18n.tl(Message.MAINTENANCE, player.getLastLocale())));
                return;
            }
            if (pendingConnection.getUniqueId() != null && pendingConnection.getName() != null) {
                if (player.getUniqueId() == null || player.getUniqueId() != pendingConnection.getUniqueId()) {
                    player.setUniqueId(pendingConnection.getUniqueId());
                }
                player.setName(pendingConnection.getName());
                if (pendingConnection.isOnlineMode() || player.isBedrock()) {
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
