package io.github.beelzebu.matrix.tasks;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.DiscordRankUpdateMessage;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.util.ErrorCodes;
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
                event.getPlayer().disconnect(new TextComponent("Internal error: " + ErrorCodes.NULL_PLAYER.getId()));
                return;
            }
            if (event.getPlayer().hasPermission("matrix.admin")) {
                player.setAdmin(true);
            } else if (player.isAdmin()) {
                player.setAdmin(false);
            }
            player.setIP(event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress());
            if (player.getLastLocale() == null) {
                player.setLastLocale(event.getPlayer().getLocale());
            }
            if (event.getPlayer().hasPermission("matrix.vip1")) {
                new DiscordRankUpdateMessage(player.getUniqueId(), DiscordRankUpdateMessage.DiscordRankType.VIP, DiscordRankUpdateMessage.Action.ADD);
            } else {
                new DiscordRankUpdateMessage(player.getUniqueId(), DiscordRankUpdateMessage.DiscordRankType.VIP, DiscordRankUpdateMessage.Action.REMOVE);
            }
            Matrix.getAPI().getPlayers().add(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
            Matrix.getLogger().debug(e);
        }
    }
}