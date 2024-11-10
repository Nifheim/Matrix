package net.nifheim.matrix.velocity.listener.tasks;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.player.meta.PlayerMetaInjector;
import net.nifheim.matrix.common.scheduler.Throwing;
import net.nifheim.matrix.common.util.ErrorCodes;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class LoginTask extends AbstractAuthTask implements Throwing.Runnable {

    private final LoginEvent event;

    public LoginTask(Logger logger, LoginEvent event, PlayerManagerImpl<Player> playerManager) {
        super(logger, playerManager);
        this.event = event;
    }

    @Override
    public void run() {
        Player velocityPlayer = event.getPlayer();
        String name = Objects.requireNonNull(velocityPlayer.getUsername(), "name");
        UUID uniqueId = Objects.requireNonNull(velocityPlayer.getUniqueId(), "uniqueId");
        MongoMatrixPlayer player = null;
        try {
            logger.info("Processing login for {} ({})", name, uniqueId);
            // validate username
            // TODO: check floodgate velocity compatibility
            // !FloodgateApi.getInstance().isFloodgatePlayer(uniqueId)
            if (!name.matches("^\\w{3,16}$")) {
                String goodName = name.replaceAll("\\W", "");
                event.setResult(ResultedEvent.ComponentResult.denied(
                        Component.text("\n" +
                                "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                                "Try using: " + goodName + "\n" +
                                "\n" +
                                "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                                "Intenta usando: " + goodName)
                ));
                return;
            }
            player = getPlayer(uniqueId, name);
            if (player == null) { // new player, so we need to create it
                player = new MongoMatrixPlayer(uniqueId, name);
                player.setLastLocale("es");
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
            player.setLastLogin(new Date());
            if (playerManager.getCacheProvider().getLocallyCached(player.getId()).isEmpty()) {
                playerManager.loginSync(player, event.getPlayer().getRemoteAddress().getAddress());
            }
            playerManager.getMetaInjector().setMeta(event.getPlayer(), PlayerMetaInjector.ID_KEY, player.getId());
        } catch (Exception e) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text("There was a problem processing your login, error code: " + ErrorCodes.UNKNOWN.getId())));
            logger.warn("There was a problem processing the login for %s (%s)".formatted(name, uniqueId), e);
        } finally {
            playerManager.saveSync(player);
        }
    }
}
