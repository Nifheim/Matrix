package net.nifheim.matrix.auth.proxy.listener.messaging;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.nifheim.matrix.api.MatrixAPI;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.auth.proxy.config.ConfigurationFile;
import net.nifheim.matrix.common.messaging.message.FieldUpdateMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class LoginListener extends MessageListener<FieldUpdateMessage> {

    private static final String LOGIN_FIELD = "loggedIn";
    private final Logger logger;
    private final ConfigurationFile configuration;
    private final ProxyServer server;
    private final MatrixAPI api;

    public LoginListener(Logger logger, ConfigurationFile configuration, ProxyServer server) {
        super(StandardChannel.UPDATE_FIELD);
        this.logger = logger;
        this.configuration = configuration;
        this.server = server;
        api = MatrixProvider.getAPI();
    }

    @Override
    public void onMessage(@NotNull FieldUpdateMessage message) {
        if (!Objects.equals(message.getField(), LOGIN_FIELD)) {
            return;
        }
        boolean value = message.getValue(boolean.class);
        if (value) {
            logger.info("Received logged in message for player with id {}", message.getPlayerId());
            api.getPlayerManager().getPlayerById(message.getPlayerId()).thenAccept(matrixPlayer -> {
                if (matrixPlayer == null) {
                    logger.error("Could not find player with id {}", message.getPlayerId());
                    return;
                }
                Optional<Player> optionalPlayer = server.getPlayer(matrixPlayer.getUniqueId());
                if (optionalPlayer.isEmpty()) {
                    logger.error("Player {} ({}) could not be found on proxy, skipping", matrixPlayer.getName(), message.getPlayerId());
                    return;
                }
                if (matrixPlayer.isLoggedIn()) {
                    logger.info("Player {} is already logged in", matrixPlayer.getName());
                } else {
                    logger.info("Player {} has just logged in", matrixPlayer.getName());
                }
                Player player = optionalPlayer.get();
                String targetServerName = configuration.getString("login.target");
                Optional<RegisteredServer> target = server.getServer(targetServerName);
                if (target.isEmpty()) {
                    error(player, "Could not find target server {} for {}", targetServerName, matrixPlayer.getName());
                    return;
                }
                player.sendMessage(Component.text("Iniciando sesión...").color(TextColor.color(0x767676)));
                player.createConnectionRequest(target.get()).connect().thenAccept(result -> {
                    if (!result.isSuccessful()) {
                        error(player, "Could not connect to target server {} for {}, status {}", targetServerName, player.getUsername(), result.getStatus());
                    } else {
                        player.sendMessage(Component.text("¡Sesión iniciada correctamente!").color(TextColor.color(0x3FFF59)));
                    }
                });
            });
        }
    }

    private void error(Player player, String message, Object... params) {
        logger.error(message, params);
        player.disconnect(Component.newline().append(Component.text("Ha ocurrido un problema mientras intentábamos iniciar tu sesión, intenta más tarde")));
    }
}
