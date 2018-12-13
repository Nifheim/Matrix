package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.listener.LoginListener;
import java.util.Objects;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class PreLoginTask implements Runnable {

    private final MatrixBungee plugin;
    private final PreLoginEvent event;
    private final MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (!event.getConnection().getName().matches("^\\w{3,16}$")) {
                event.setCancelReason(new TextComponent("\n" +
                        "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                        "Try using: " + event.getConnection().getName().replaceAll("[^\\w]", "") + "\n" +
                        "\n" +
                        "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                        "Intenta usando: " + event.getConnection().getName().replaceAll("[^\\w]", "")));
                event.setCancelled(true);
                return;
            }
            if (LoginListener.isProxy(event.getConnection().getAddress().getAddress().getHostAddress())) {
                Matrix.getAPI().getPlugin().ban(event.getConnection().getAddress().getAddress().getHostAddress());
                return;
            }
            if (player != null) {
                if (!Objects.equals(Matrix.getAPI().getPlayer(event.getConnection().getName()).getUniqueId(), event.getConnection().getUniqueId()) || !Objects.equals(player.getUniqueId(), event.getConnection().getUniqueId())) {
                    event.setCancelReason(new TextComponent("\n" +
                            "Your UUID doesn't match with the UUID associated to your name in our database.\n" +
                            "This login attempt was recorded for security reasons."));
                    event.setCancelled(true);
                    return;
                }
                if (player.isPremium()) {
                    event.getConnection().setOnlineMode(true);
                }
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getMessage()));
            event.setCancelled(true);
            e.printStackTrace();
        } finally {
            event.completeIntent(plugin);
        }
    }
}
