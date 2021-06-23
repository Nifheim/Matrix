package com.github.beelzebu.matrix.bungee.listener.tasks;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.ErrorCodes;
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
public class PreLoginTask implements Runnable {

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
            if (profile != null || host.startsWith("premium.")) {
                Matrix.getLogger().info("Premium account detected for " + name);
                if (profile != null) {
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayer(profile.getId()).join();
                } else {
                    player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(event.getConnection().getName()).join();
                }
                if (player != null) {
                    player.setPremium(true);
                    player.setName(name);
                    if (!player.isBedrock()) {
                        // event.getConnection().setUniqueId(profile.getId()); // TODO: check uuid was being forced for online mode connections
                        event.getConnection().setOnlineMode(true);
                    }
                } else {
                    Matrix.getLogger().info(name + " is not premium on the server");
                }
            }
            if (player == null) {
                player = (MongoMatrixPlayer) api.getPlayerManager().getPlayerByName(name).join();
                if (player == null) {
                    if (profile != null) {
                        // TODO: set new premium players as premium on first login
                        /*
                        player = new MongoMatrixPlayer(profile.getId(), name);
                        player.setPremium(true);
                        player.save().join();
                        event.setCancelReason(new TextComponent("Estamos validando que no seas un bot, por favor reconecta..."));
                        event.setCancelled(true);
                        return;
                         */
                    } else {
                        player = new MongoMatrixPlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()), name);
                    }
                    player.setLastLocale("es");
                    player.save().join(); // block until player is saved
                } else {
                    if (player.isPremium() && profile != null) {
                        player.setUniqueId(profile.getId());
                    }
                }
                if (fetchedProfile && profile == null) {
                    player.setPremium(false);
                }
            }
            if (player.isPremium() && !player.isBedrock()) {
                event.getConnection().setOnlineMode(true);
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
