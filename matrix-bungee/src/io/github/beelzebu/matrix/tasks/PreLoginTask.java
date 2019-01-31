package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Objects;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class PreLoginTask implements Runnable {

    private final MatrixBungeeBootstrap plugin;
    private final PreLoginEvent event;
    private final MatrixPlayer player;

    @Override
    public void run() {
        try {
            String host = event.getConnection().getVirtualHost().getHostName();
            if (!Objects.equals(host, "mc.nifheim.net")) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using mc.nifheim.net\n" +
                        "\n" +
                        "Por favor ingresa usando mc.nifheim.net"));
                return;
            }
            if (event.getConnection().getName() == null || !event.getConnection().getName().matches("^\\w{3,16}$")) {
                event.setCancelReason(new TextComponent("\n" +
                        "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                        "Try using: " + event.getConnection().getName().replaceAll("[^\\w]", "") + "\n" +
                        "\n" +
                        "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                        "Intenta usando: " + event.getConnection().getName().replaceAll("[^\\w]", "")));
                event.setCancelled(true);
                return;
            }
            /**
             * if (LoginListener.isProxy(event.getConnection().getAddress().getAddress().getHostAddress())) {
             Matrix.getAPI().getPlugin().ban(event.getConnection().getAddress().getAddress().getHostAddress());
             return;
             }
             */
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getLocalizedMessage()));
            event.setCancelled(true);
            Matrix.getAPI().debug(e);
        } finally {
            event.completeIntent(plugin);
        }
    }
}
