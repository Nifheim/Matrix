package net.nifheim.matrix.velocity.listener.tasks;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.RateLimitException;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.scheduler.Throwing;
import net.nifheim.matrix.common.util.ErrorCodes;
import net.nifheim.matrix.velocity.bootstrap.MatrixVelocityBootstrap;
import net.nifheim.matrix.velocity.util.LoginState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class PreLoginTask extends AbstractAuthTask implements Throwing.Runnable {

    private final PreLoginEvent event;
    private final LoginState state;
    @Nullable
    private MatrixPlayer player;
    private Profile profile;

    public PreLoginTask(Logger logger, PreLoginEvent event, LoginState state, PlayerManager playerManager) {
        super(logger, playerManager);
        this.event = event;
        this.state = state;
    }

    @Override
    public void run() {
        try {
            String name = Objects.requireNonNull(event.getUsername(), "name");
            logger.info("Processing pre login for {}", name);
            // validate the hostname that the player tried to use to connect
            InetSocketAddress address = event.getConnection().getVirtualHost().orElse(null);
            if (address == null) { // hostname is not present, cancel connection
                logger.error("Hostname not present, canceling connection for {}", name);
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("\n" + "Please join using " + MatrixCommon.DOMAIN_NAME + "\n" + "\n" + "Por favor ingresa usando " + MatrixCommon.DOMAIN_NAME)));
                return;
            }
            String host = address.getHostString();
            boolean badDomain = true;
            for (String domain : MatrixCommon.DOMAIN_NAMES) {
                if (host.endsWith(domain)) {
                    badDomain = false;
                    break;
                }
            }
            if (badDomain) { // hostname not in our whitelist, it might be a bot attack, cancel connection
                logger.error("Hostname {} not in whitelist, cancelling connection for {}", host, name);
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Please join using " + MatrixCommon.DOMAIN_NAME + "\n" + "\n" + "Por favor ingresa usando " + MatrixCommon.DOMAIN_NAME)));
                return;
            }
            player = playerManager.getPlayerSync(name);
            if (state == LoginState.PRE_LOGIN && player != null) {
                logger.warn("Stored {} for {} checking invalid premium.", LoginState.PRE_LOGIN, name);
                if (player.isPremium() && !player.isRegistered()) {
                    player.setPremium(false);
                }
            }
            if (player != null) { // player already exists in our database
                if (!Objects.equals(name, player.getName())) { // username doesn't match
                    if (player.isPremium()) { // player is premium, so we can safely update the username.
                        updatePremiumPlayer(player.getUniqueId(), name);
                    } else { // we must cancel the login to avoid data loss
                        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You must login using the name").appendNewline().append(Component.empty().append(Component.text(player.getName() + "\n").decorate(TextDecoration.BOLD))).appendNewline().append(Component.text("Check for uppercase and lowercase.")).appendNewline().append(Component.text("Debes ingresar usando el nombre")).appendNewline().append(Component.empty().append(Component.text(player.getName() + "\n").decorate(TextDecoration.BOLD))).appendNewline().append(Component.text("Revisa mayúsculas y minúsculas."))));
                    }
                }
            } else { // new player handling
                boolean fetchedProfile = false;
                profile = null;
                try {
                    profile = MatrixVelocityBootstrap.RESOLVER.findProfile(name).orElse(null);
                    fetchedProfile = true;
                } catch (RateLimitException | IOException e) {
                    logger.error("Can't fetch profile for %s".formatted(name), e);
                }
                if (profile != null) { // premium account exists with the name of this player
                    logger.info("Premium profile detected for {}", name);
                    player = getPlayer(profile.getId(), name);
                    if (player != null) { // we have a player with the found uuid for the name
                        updatePremiumPlayer(profile.getId(), name);
                    } else {
                        logger.info("Player not found for {} ({})", name, profile.getId());
                    }
                } else if (!fetchedProfile) {
                    logger.warn("Can't fetch profile for " + name);
                }
            }
        } catch (Exception e) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("There was a problem processing your pre login, error code: " + (player == null ? ErrorCodes.NULL_PLAYER.getId() : ErrorCodes.UNKNOWN.getId()))));
            logger.error("An exception has occurred while processing pre login for %s".formatted(event.getUsername()), e);
        } finally {
            if (player != null) {
                if (player.isPremium()) {
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                }
            } else if (profile != null) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            }
        }
    }

    public @Nullable Profile getProfile() {
        return profile;
    }

    public @Nullable MatrixPlayer getPlayer() {
        return player;
    }

    private void updatePremiumPlayer(UUID uniqueId, String name) {
        Objects.requireNonNull(player, "player");
        logger.info("Updating premium player for {} ({})", name, uniqueId);
        player.setPremium(true);
        player.setUniqueId(uniqueId);
        player.setName(name);
    }
}
