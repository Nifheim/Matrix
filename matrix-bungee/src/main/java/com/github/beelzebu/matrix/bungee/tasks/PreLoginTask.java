package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.RateLimitException;
import java.io.IOException;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
public class PreLoginTask implements IndioLoginTask {

    private final MatrixBungeeAPI api;
    private final PreLoginEvent event;
    private MongoMatrixPlayer player;

    public PreLoginTask(MatrixBungeeAPI api, PreLoginEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        try {
            Profile profile = null;
            try {
                profile = RESOLVER.findProfile(event.getConnection().getName()).orElse(null);
            } catch (RateLimitException | IOException e) {
                e.printStackTrace();
            }
            if (profile != null) {
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(profile.getId()).join();
                if (player != null) {
                    if (player.isPremium()) {
                        if (event.getConnection().getName() != null) {
                            player.setName(event.getConnection().getName());
                        }
                        if (!player.isBedrock()) {
                            event.getConnection().setOnlineMode(true);
                        }
                    }
                }
            }
            if (player == null) {
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(event.getConnection().getName()).join();
                if (player == null) {
                    player = new MongoMatrixPlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getConnection().getName()).getBytes()), event.getConnection().getName());
                    player.save().join(); // block until player is saved
                    player.setOption(PlayerOptionType.SPEED, true);
                    player.setLastLocale("es");
                }
            }
            String host = event.getConnection().getVirtualHost().getHostName();
            if (host == null) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                return;
            }
            boolean badDomain = true;
            for (String domain : MatrixAPIImpl.DOMAIN_NAMES) {
                if (host.endsWith(domain)) {
                    badDomain = false;
                    break;
                }
            }
            if (badDomain) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                return;
            }
            if (event.getConnection().getName() == null || !event.getConnection().getName().matches("^\\w{3,16}$")) {
                String goodName = event.getConnection().getName().replaceAll("[^\\w]", "");
                event.setCancelReason(new TextComponent("\n" +
                        "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                        "Try using: " + goodName + "\n" +
                        "\n" +
                        "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                        "Intenta usando: " + goodName));
                event.setCancelled(true);
                return;
            }
            for (String domain : MatrixAPIImpl.DOMAIN_NAMES) {
                if (host.equals("premium." + domain)) {
                    if (!player.isBedrock()) {
                        event.getConnection().setOnlineMode(true);
                    }
                    if (!player.isPremium()) {
                        player.setPremium(true);
                    }
                    break;
                }
            }
            if (player.isPremium() && !player.isBedrock()) {
                event.getConnection().setOnlineMode(true);
            }
            api.getDatabase().save(player.getId(), player);
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("There was a problem processing your login, error code: " + (player == null ? ErrorCodes.NULL_PLAYER.getId() : ErrorCodes.UNKNOWN.getId())));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }
}
