package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.exception.LoginException;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.LoginState;
import com.github.beelzebu.matrix.util.MetaInjector;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;

/**
 * @author Beelzebu
 */
public class PostLoginTask implements Throwing.Runnable {

    private final MatrixBungeeAPI api;
    private final PostLoginEvent event;
    private final boolean profile;

    public PostLoginTask(MatrixBungeeAPI api, PostLoginEvent event, boolean profile) {
        this.api = api;
        this.event = event;
        this.profile = profile;
    }

    @Override
    public void run() {
        MatrixPlayer player = api.getPlayerManager().getPlayer(event.getPlayer().getUniqueId()).join();
        try {
            if (player == null) {
                player = api.getPlayerManager().getPlayerByName(event.getPlayer().getName()).join();
                if (player == null) {
                    throw new LoginException(ErrorCodes.NULL_PLAYER, LoginState.POST_LOGIN);
                }
                if (!player.isPremium() && player.getUniqueId().version() == 4) {
                    player.setUniqueId(event.getPlayer().getUniqueId()).join();
                }
            }
            Matrix.getLogger().debug("Processing post login for " + player.getName() + " " + player.getId());
            player.setIP(event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress());
            if (!player.isPremium() && profile) {
                MatrixPlayer finalPlayer = player;
                api.getPlugin().getBootstrap().getScheduler().asyncLater(() -> finalPlayer.sendMessage(I18n.tl(Message.PREMIUM_SUGGESTION, finalPlayer.getLastLocale())), 5, TimeUnit.SECONDS);
            }
            api.getPlayerManager().getMetaInjector().setMeta(event.getPlayer(), MetaInjector.ID_KEY, player.getId());
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent("There was a problem processing your post login, error code: " + ErrorCodes.UNKNOWN.getId()));
            Matrix.getLogger().debug(e);
        }
    }
}