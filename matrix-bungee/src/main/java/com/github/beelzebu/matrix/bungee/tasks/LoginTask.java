package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
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

    private final MatrixBungeeAPI api;
    private final LoginEvent event;
    private boolean firstJoin = false;

    public LoginTask(MatrixBungeeAPI api, LoginEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        MongoMatrixPlayer player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(event.getConnection().getUniqueId()).join();
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
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(profile.getId()).join();
                    if (player != null) {
                        if (event.getConnection().getName() != null && !Objects.equals(player.getName(), event.getConnection().getName())) {
                            player.setName(event.getConnection().getName());
                        }
                    }
                }
                if (player == null) {
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(event.getConnection().getName()).join();
                }
            }
        }
        try {
            PendingConnection pendingConnection = event.getConnection();
            if (player == null) {
                player = new MongoMatrixPlayer(pendingConnection.getUniqueId(), pendingConnection.getName());
                player.save().join();
                player.setLastLocale("es");
                firstJoin = true;
            }
            if (FloodgateAPI.isBedrockPlayer(player.getUniqueId())) {
                player.setBedrock(true);
            }
            if (!player.isPremium() && !Objects.equals(player.getUniqueId(), UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getConnection().getName()).getBytes()))) {
                event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.UUID_DONTMATCH.getId() + "\n\nYour UUID doesn't match with the UUID associated to your name in our database.\nThis login attempt was recorded for security reasons."));
                event.setCancelled(true);
                api.getDatabase().addFailedLogin(event.getConnection().getUniqueId(), event.getConnection().getName(), "error login bungee");
                return;
            }
            if (!event.getConnection().getName().equalsIgnoreCase("Beelzebu") && api.getMaintenanceManager().isMaintenance() && !player.isAdmin()) {
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
                if (firstJoin) {
                    player.setOption(PlayerOptionType.SPEED, true);
                }
                player.setLastLogin(new Date());
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId()));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }
}
