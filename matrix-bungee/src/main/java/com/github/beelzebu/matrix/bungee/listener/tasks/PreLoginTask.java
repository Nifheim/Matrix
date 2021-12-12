package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.LoginState;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.RateLimitException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class PreLoginTask implements Throwing.Runnable {

    protected final MatrixBungeeAPI api;
    protected final PreLoginEvent event;
    protected final LoginState state;
    private MongoMatrixPlayer player;
    private Profile profile;

    public PreLoginTask(MatrixBungeeAPI api, PreLoginEvent event, LoginState state) {
        this.api = api;
        this.event = event;
        this.state = state;
    }

    @Override
    public void run() {
        try {
            String name = Objects.requireNonNull(event.getConnection().getName(), "name");
            Matrix.getLogger().debug("Processing pre login for " + name);
            // validate the hostname that the player tried to use to connect
            String host = event.getConnection().getVirtualHost().getHostName();
            if (host == null) { // hostname not present, cancel connection
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                return;
            }
            boolean badDomain = true;
            for (String domain : MatrixAPIImpl.DOMAIN_NAMES) {
                if (host.endsWith(domain)) {
                    badDomain = false;
                    break;
                }
            }
            if (badDomain) { // hostname not in our whitelist, may be bot attack, cancel connection
                event.setCancelled(true);
                event.setCancelReason(new TextComponent("\n" +
                        "Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\n" +
                        "\n" +
                        "Por favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                return;
            }
            player = (MongoMatrixPlayer) Matrix.getAPI().getPlayerManager().getPlayerByName(name).join();
            if (state == LoginState.PRE_LOGIN && player != null) {
                Matrix.getLogger().info("Stored " + LoginState.PRE_LOGIN + " for " + name + " checking invalid premium.");
                if (player.isPremium() && !player.isRegistered()) {
                    player.setPremium(false).join();
                    event.getConnection().setOnlineMode(false);
                }
            }
            if (player != null) { // player already exists in our database
                if (!Objects.equals(name, player.getName())) { // username doesn't match
                    if (player.isPremium()) { // player is premium, so we can safely update the username.
                        player.setName(name).join();
                    } else { // we must cancel the login to avoid data loss
                        BaseComponent[] reason = new ComponentBuilder("Debes ingresar usando el nombre\n").append(player.getName() + "\n").bold(true).append("Revisa mayúsculas y minúsculas.").bold(false).create();
                        event.setCancelReason(reason);
                        event.setCancelled(true);
                        return;
                    }
                }
            } else { // new player handling
                boolean fetchedProfile = false;
                profile = null;
                try {
                    profile = MatrixBungeeAPI.RESOLVER.findProfile(name).orElse(null);
                    fetchedProfile = true;
                } catch (RateLimitException | IOException e) {
                    e.printStackTrace();
                }
                if (profile != null) { // premium account exists with the name of this player
                    Matrix.getLogger().info("Premium account detected for " + name);
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(profile.getId()).join();
                    if (player != null) { // we have a player with the found uuid for the name
                        player.setPremium(true).join();
                        player.setUniqueId(profile.getId()).join();
                        player.setName(name).join();
                    } else {
                        player = new MongoMatrixPlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()), name);
                        player.setPremium(true).join();
                        player.setLastLocale("es").join();
                        player.save().join();
                    }
                } else if (!fetchedProfile) {
                    Matrix.getLogger().warn("Can't fetch profile for " + name);
                }
            }
            if (player != null) {
                if (player.isPremium()) {
                    event.getConnection().setOnlineMode(true);
                    player.setLoggedIn(true).join();
                }
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("There was a problem processing your pre login, error code: " + (player == null ? ErrorCodes.NULL_PLAYER.getId() : ErrorCodes.UNKNOWN.getId())));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }

    public @Nullable Profile getProfile() {
        return profile;
    }
}
