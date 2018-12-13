package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.Message;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.BungeeMatrixPlayer;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class LoginTask implements Runnable {

    private final MatrixBungee plugin;
    private final LoginEvent event;
    private MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (player == null) {
                player = new BungeeMatrixPlayer(event.getConnection().getUniqueId()).save();
            }
            if (plugin.isMaintenance()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(Matrix.getAPI().getString(Message.MAINTENANCE, player.getLastLocale())));
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getMessage()));
            event.setCancelled(true);
        } finally {
            event.completeIntent(plugin);
        }
    }
}
