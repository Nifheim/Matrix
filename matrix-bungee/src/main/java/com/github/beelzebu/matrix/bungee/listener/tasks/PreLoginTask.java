package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.util.Throwing;
import com.github.beelzebu.matrix.exception.LoginException;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.LoginState;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import com.github.games647.craftapi.resolver.RateLimitException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class PreLoginTask implements Throwing.Runnable {

    private static final MojangResolver RESOLVER = new MojangResolver();
    private final MatrixBungeeAPI api;
    private final PreLoginEvent event;
    private MongoMatrixPlayer player;

    public PreLoginTask(MatrixBungeeAPI api, PreLoginEvent event) {
        this.api = api;
        this.event = event;
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
            // validate username
            if (!name.matches("^\\w{3,16}$")) {
                String goodName = name.replaceAll("[^\\w]", "");
                event.setCancelReason(new TextComponent("\n" +
                        "Your username is invalid, it must be alphanumeric and can't contain spaces.\n" +
                        "Try using: " + goodName + "\n" +
                        "\n" +
                        "Tu nombre es inválido, debe ser alfanumérico y no puede contener espacios.\n" +
                        "Intenta usando: " + goodName));
                event.setCancelled(true);
                return;
            }
            boolean fetchedProfile = false;
            Profile profile = null;
            try {
                profile = RESOLVER.findProfile(name).orElse(null);
                fetchedProfile = true;
            } catch (@NotNull RateLimitException | IOException e) {
                e.printStackTrace();
            }
            if (profile != null) { // premium account exists with the name of this player
                Matrix.getLogger().info("Premium account detected for " + name);
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(profile.getId()).join();
                if (player != null) { // we have a player with the found uuid for the name
                    if (!player.isRegistered()) { // player is not registered, and exists here, so we'll register the account
                        player.setPremium(true);
                        player.setUniqueId(profile.getId());
                    }
                } else { // there is no player with the provided uuid, check for an account with the same name
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(name).join();
                    if (player != null) { // account with the name found
                        if (player.isPremium()) { // player is premium, may be an old account for another premium player, so force new uuid
                            player.setUniqueId(profile.getId());
                        } else { // there is no player with the name or uuid, so is a new player, we'll create an account below
                            Matrix.getLogger().info(name + " is not premium on the server, fetched by name");
                        }
                    } else {
                        Matrix.getLogger().info(name + " doesn't exists in our database.");
                    }
                }
            }
            if (player == null) {
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(name).join();
                if (player == null) {
                    player = new MongoMatrixPlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()), name);
                    player.save().join(); // block until player is saved
                    player.setLastLocale("es");
                } else {
                    if (player.isPremium() && profile != null) {
                        player.setUniqueId(profile.getId());
                    }
                    player.setName(name);
                }
                if (fetchedProfile && profile == null) {
                    player.setPremium(false);
                }
            }
            if (player == null) {
                throw new LoginException(ErrorCodes.NULL_PLAYER, LoginState.PRE_LOGIN);
            }
            player.setLoggedIn(false);
            if (player.isPremium()) {
                if (!player.getName().equals(name)) {
                    player.setName(name);
                }
                event.getConnection().setOnlineMode(true);
                player.setLoggedIn(true);
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("There was a problem processing your pre login, error code: " + (player == null ? ErrorCodes.NULL_PLAYER.getId() : ErrorCodes.UNKNOWN.getId())));
            event.setCancelled(true);
            Matrix.getLogger().debug(e);
        } finally {
            event.completeIntent(api.getPlugin().getBootstrap());
        }
    }
}
