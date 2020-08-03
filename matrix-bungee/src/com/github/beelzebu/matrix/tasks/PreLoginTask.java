package com.github.beelzebu.matrix.tasks;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import java.util.Objects;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
public class PreLoginTask implements Runnable {

    private final MatrixBungeeBootstrap plugin;
    private final PreLoginEvent event;
    private final MatrixPlayer player;

    public PreLoginTask(MatrixBungeeBootstrap plugin, PreLoginEvent event, MatrixPlayer player) {
        this.plugin = plugin;
        this.event = event;
        if (player == null) {
            // connection name can be null
            if (event.getConnection().getName() != null) {
                this.player = Matrix.getAPI().getPlayer(event.getConnection().getName());
            } else {
                this.player = null;
            }
        } else {
            this.player = player;
        }
    }

    @Override
    public void run() {
        try {
            String host = event.getConnection().getVirtualHost().getHostName();
            if (host == null || (!host.endsWith(Matrix.DOMAIN) && !host.equals("play.pixelnetwork.net"))) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + Matrix.IP + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + Matrix.IP));
                return;
            }
            if (!Objects.equals(event.getConnection().getVirtualHost().getPort(), 25565)) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + Matrix.IP + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + Matrix.IP));
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
            if (host.equals("premium." + Matrix.DOMAIN)) {
                event.getConnection().setOnlineMode(true);
                if (player != null) {
                    if (!player.isPremium()) {
                        player.setPremium(true);
                    }
                }
            }
            if (player != null) {
                if (player.isPremium()) {
                    event.getConnection().setOnlineMode(true);
                }
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
            event.completeIntent(plugin);
        }
    }
}
