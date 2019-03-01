package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class DisconnectTask implements Runnable {

    private final PlayerDisconnectEvent event;
    private final MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (player.isAuthed()) {
                player.setAuthed(false);
            }
            if (player.isAdmin() && !event.getPlayer().hasPermission("matrix.admin")) {
                player.setAdmin(false);
            }
            if (player.getLastLogin() != null && player.getRegistration() != null && player.getRegistration().before(player.getLastLogin())) {
                ((MongoMatrixPlayer) player).setRegistration(player.getLastLogin());
            }
            player.setLastLogin(new Date());
            Matrix.getAPI().getPlayers().remove(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
            Matrix.getLogger().debug(e);
        }
    }
}