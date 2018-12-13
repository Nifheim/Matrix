package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.player.BungeeMatrixPlayer;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class PreLoginTask implements Runnable {

    private final MatrixBungee plugin;
    private final PreLoginEvent e;
    private MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (!e.getConnection().getName().matches("^\\w")) {
                e.setCancelReason(new TextComponent("\n" +
                        "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                        "Try using: " + e.getConnection().getName().replaceAll("[^\\w]", "") + "\n" +
                        "\n" +
                        "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                        "Intenta usando: " + e.getConnection().getName().replaceAll("[^\\w]", "")));
                e.setCancelled(true);
                return;
            }
            if (LoginListener.isProxy(e.getConnection().getAddress().getAddress().getHostAddress())) {
                Matrix.getAPI().getPlugin().ban(e.getConnection().getAddress().getAddress().getHostAddress());
                return;
            }
            if (player == null) {
                player = Optional.ofNullable(Matrix.getAPI().getPlayer(e.getConnection().getUniqueId())).orElse(new BungeeMatrixPlayer(e.getConnection().getUniqueId()).save());
            }
            if (!Objects.equals(Matrix.getAPI().getPlayer(e.getConnection().getName()).getUniqueId(), e.getConnection().getUniqueId()) || !Objects.equals(player.getUniqueId(), e.getConnection().getUniqueId())) {
                e.setCancelReason(new TextComponent("\n" +
                        "Your UUID doesn't match with the UUID associated to your name in our database.\n" +
                        "This login attempt was recorded for security reasons."));
                e.setCancelled(true);
                return;
            }
            if (player.isPremium()) {
                e.getConnection().setOnlineMode(true);
            }
        } catch (Exception e) {
            this.e.setCancelReason(new TextComponent(e.getMessage()));
            this.e.setCancelled(true);
        } finally {
            e.completeIntent(plugin);
        }
    }
}
