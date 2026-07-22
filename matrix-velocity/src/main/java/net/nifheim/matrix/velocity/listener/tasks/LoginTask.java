package net.nifheim.matrix.velocity.listener.tasks;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.scheduler.Throwing;
import net.nifheim.matrix.common.util.ErrorCodes;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class LoginTask extends AbstractAuthTask implements Throwing.Runnable {

    private final LoginEvent event;

    public LoginTask(Logger logger, LoginEvent event, PlayerManager playerManager) {
        super(logger, playerManager);
        this.event = event;
    }

    @Override
    public void run() {
        Player velocityPlayer = event.getPlayer();
        String name = Objects.requireNonNull(velocityPlayer.getUsername(), "name");
        UUID uniqueId = Objects.requireNonNull(velocityPlayer.getUniqueId(), "uniqueId");
        MatrixPlayer player;
        try {
            logger.info("Processing login for {} ({})", name, uniqueId);
            // validate username
            // TODO: check floodgate velocity compatibility
            // !FloodgateApi.getInstance().isFloodgatePlayer(uniqueId)
            if (!name.matches("^\\w{3,16}$")) {
                String goodName = name.replaceAll("\\W", "");
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text("\n" + "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" + "Try using: " + goodName + "\n" + "\n" + "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" + "Intenta usando: " + goodName)));
                return;
            }
            player = getPlayer(uniqueId, name);
            if (player == null) { // new player, so we need to create it
                player = playerManager.createPlayer(uniqueId, name, velocityPlayer.getEffectiveLocale());
                logger.warn("New player: {}", name);
            }
            logger.info("HexId for {}: {}", name, player.getId());
            if (velocityPlayer.isOnlineMode()) {
                player.setPremium(true); // set premium before updating uuid
                player.setLoggedIn(true);
                player.setRegistered(true);
            }
            if (uniqueId.version() == 4 && player.getUniqueId() != uniqueId) {
                player.setUniqueId(uniqueId);
            }
            playerManager.getCacheProvider().add(player);
        } catch (Exception e) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId())));
            logger.warn("There was a problem processing the login for %s (%s)".formatted(name, uniqueId), e);
        }
    }
}
