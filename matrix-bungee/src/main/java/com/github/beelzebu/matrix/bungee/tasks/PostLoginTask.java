package com.github.beelzebu.matrix.bungee.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.MetaInjector;
import java.util.Objects;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;

/**
 * @author Beelzebu
 */
public class PostLoginTask implements IndioLoginTask {

    private final MatrixBungeeAPI api;
    private final PostLoginEvent event;

    public PostLoginTask(MatrixBungeeAPI api, PostLoginEvent event) {
        this.api = api;
        this.event = event;
    }

    @Override
    public void run() {
        MatrixPlayer player = api.getPlayerManager().getPlayer(event.getPlayer().getUniqueId()).join();
        try {
            Objects.requireNonNull(player, "player");
            if (event.getPlayer().hasPermission("matrix.admin")) {
                player.setAdmin(true);
            } else if (player.isAdmin()) {
                player.setAdmin(false);
            }
            player.setIP(event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress());
            if (!player.isPremium()) {
                player.sendMessage(I18n.tl(Message.PREMIUM_SUGGESTION, player.getLastLocale()));
            }
            api.getMetaInjector().setMeta(event.getPlayer(), MetaInjector.ID_KEY, player.getId());
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId()));
            Matrix.getLogger().debug(e);
        }
    }
}