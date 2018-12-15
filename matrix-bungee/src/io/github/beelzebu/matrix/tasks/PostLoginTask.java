package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class PostLoginTask implements Runnable {

    private final PostLoginEvent event;
    private final MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (player == null) {
                event.getPlayer().disconnect(new TextComponent("Internal error."));
                return;
            }
            if (event.getPlayer().hasPermission("nifheim.admin")) {
                player.setAdmin(false);
            } else if (!event.getPlayer().hasPermission("nifheim.admin") && player.isAdmin()) {
                player.setAdmin(false);
            }
            Matrix.getAPI().getPlayers().add(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getMessage()));
            e.printStackTrace();
        }
    }
}