package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.Message;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
@RequiredArgsConstructor
public class LoginTask implements Runnable {

    private final MatrixBungee plugin;
    private final MatrixPlayer player;
    private final LoginEvent event;

    @Override
    public void run() {
        try {
            if (player == null) {
                event.setCancelReason(new TextComponent());
                event.setCancelled(true);
                return;
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
