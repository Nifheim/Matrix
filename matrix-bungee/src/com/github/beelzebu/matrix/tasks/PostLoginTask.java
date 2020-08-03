package com.github.beelzebu.matrix.tasks;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.messaging.message.DiscordRankUpdateMessage;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;

/**
 * @author Beelzebu
 */
public class PostLoginTask implements Runnable {

    private final PostLoginEvent event;
    private final MatrixPlayer player;

    public PostLoginTask(PostLoginEvent event, MatrixPlayer player) {
        this.event = event;
        this.player = player;
    }

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
            if (event.getPlayer().hasPermission("matrix.vip1")) {
                new DiscordRankUpdateMessage(player.getUniqueId(), DiscordRankUpdateMessage.DiscordRankType.VIP, DiscordRankUpdateMessage.Action.ADD);
            } else {
                new DiscordRankUpdateMessage(player.getUniqueId(), DiscordRankUpdateMessage.DiscordRankType.VIP, DiscordRankUpdateMessage.Action.REMOVE);
            }
            if (!player.isPremium()) {
                player.sendMessage(I18n.tl(Message.PREMIUM_SUGGESTION, player.getLastLocale()));
            }
            Matrix.getAPI().getCache().saveToCache(player);
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(e.getLocalizedMessage()));
            Matrix.getLogger().debug(e);
        }
    }
}