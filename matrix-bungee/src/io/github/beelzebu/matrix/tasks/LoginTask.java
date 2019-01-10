package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.Message;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import io.github.beelzebu.matrix.utils.ErrorCodes;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class LoginTask implements Runnable {

    private final MatrixBungeeBootstrap plugin;
    private final LoginEvent event;
    private MatrixPlayer player;

    @Override
    public void run() {
        try {
            if (player == null) {
                if (event.getConnection().getUniqueId() != null && event.getConnection().getName() != null) {
                    MatrixPlayer playerByName = Matrix.getAPI().getPlayer(event.getConnection().getName());
                    if (playerByName != null && !playerByName.isPremium() && event.getConnection().isOnlineMode()) {
                        playerByName.setUniqueId(event.getConnection().getUniqueId());
                        playerByName.setPremium(true);
                        playerByName.setRegistered(true);
                    } else {
                        player = new MongoMatrixPlayer(event.getConnection().getUniqueId(), event.getConnection().getName()).save();
                    }
                } else {
                    event.setCancelled(true);
                    event.setCancelReason(new TextComponent("Internal error: " + ErrorCodes.NULL_PLAYER.getId()));
                    return;
                }
            }
            if (plugin.isMaintenance() && !player.isAdmin()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(Matrix.getAPI().getString(Message.MAINTENANCE, player.getLastLocale())));
                return;
            }
            player.saveToRedis();
            Matrix.getAPI().getPlayers().add(player);
        } catch (Exception e) {
            event.setCancelReason(new TextComponent(e.getLocalizedMessage()));
            event.setCancelled(true);
            Matrix.getAPI().debug(e);
        } finally {
            event.completeIntent(plugin);
        }
    }
}
