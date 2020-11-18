package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.RateLimitException;
import java.io.IOException;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
public class PreLoginTask implements IndioLoginTask {

    private final MatrixBungeeBootstrap plugin;
    private final PreLoginEvent event;
    private final MongoMatrixPlayer player;

    public PreLoginTask(MatrixBungeeBootstrap plugin, PreLoginEvent event) {
        this.plugin = plugin;
        this.event = event;
        MongoMatrixPlayer player = (MongoMatrixPlayer) Matrix.getAPI().getPlayer(event.getConnection().getName());
        if (player == null) {
            Profile profile = null;
            try {
                profile = RESOLVER.findProfile(event.getConnection().getName()).orElse(null);
            } catch (RateLimitException | IOException e) {
                e.printStackTrace();
            }
            if (profile != null) {
                player = (MongoMatrixPlayer) Matrix.getAPI().getPlayer(profile.getId());
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
        }
        if (player == null) {
            Matrix.getLogger().info("Player null pre login name: " + event.getConnection().getName());
        }
        this.player = player;
    }

    @Override
    public void run() {
        try {
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
                    if (player != null && !player.isPremium()) {
                        player.setPremium(true);
                    }
                    break;
                }
            }
            if (player != null && player.isPremium() && !player.isBedrock()) {
                event.getConnection().setOnlineMode(true);
            }
            Matrix.getAPI().getCache().update(event.getConnection().getName(), event.getConnection().getUniqueId());
            /**
             * if (LoginListener.isProxy(event.getConnection().getAddress().getAddress().getHostAddress())) {
             Matrix.getAPI().getPlugin().ban(event.getConnection().getAddress().getAddress().getHostAddress());
             return;
             }
             */
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getLocalizedMessage()));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            this.event.completeIntent(this.plugin);
        }
    }
}
