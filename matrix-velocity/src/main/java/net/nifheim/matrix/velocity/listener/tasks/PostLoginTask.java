package net.nifheim.matrix.velocity.listener.tasks;

import com.velocitypowered.api.event.connection.PostLoginEvent;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.scheduler.Throwing;
import net.nifheim.matrix.common.util.ErrorCodes;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class PostLoginTask extends AbstractAuthTask implements Throwing.Runnable {

    private final PostLoginEvent event;
    private final boolean profile;

    public PostLoginTask(Logger logger, PostLoginEvent event, PlayerManager playerManager, boolean profile) {
        super(logger, playerManager);
        this.event = event;
        this.profile = profile;
    }

    @Override
    public void run() {
        logger.info("Processing post login for {} ({})", event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
        MatrixPlayer player = getPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
        Objects.requireNonNull(player, "Matrix player not found");
        try {
            if (!player.isPremium() && profile) {
                MatrixPlayer finalPlayer = player;
                // TODO: re add message about premium suggestion
                //api.getPlugin().getBootstrap().getScheduler().asyncLater(() -> finalPlayer.sendMessage(I18n.tl(Message.PREMIUM_SUGGESTION, finalPlayer.getLastLocale())), 5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            event.getPlayer().disconnect(Component.text("There was a problem processing your post login, error code: " + ErrorCodes.UNKNOWN.getId()));
            logger.warn("There was a problem processing post login for " + event.getPlayer().getUsername() + " (" + event.getPlayer().getUniqueId() + ")", e);
        }
    }
}
