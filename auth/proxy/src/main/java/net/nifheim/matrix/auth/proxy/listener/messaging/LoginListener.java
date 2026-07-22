package net.nifheim.matrix.auth.proxy.listener.messaging;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.nifheim.matrix.api.MatrixAPI;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.auth.proxy.config.ConfigurationFile;
import net.nifheim.matrix.common.messaging.message.LoginMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import org.slf4j.Logger;

public class LoginListener implements BiConsumer<LoginMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    private final Logger logger;
    private final ConfigurationFile configuration;
    private final ProxyServer server;
    private final MatrixAPI api;

    public LoginListener(Logger logger, ConfigurationFile configuration, ProxyServer server) {
        this.logger = logger;
        this.configuration = configuration;
        this.server = server;
        this.api = MatrixProvider.getAPI();
    }

    @Override
    public void accept(LoginMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        Optional<Player> optionalPlayer = server.getPlayer(LoginMessage.getPlayerUniqueId(message));
        if (optionalPlayer.isEmpty()) {
            try {
                context.acknowledge();
            } catch (IOException e) {
                logger.error("Error acknowledging login message for player {} (player not connected)", LoginMessage.getPlayerName(message), e);
            }
            return;
        }

        Player player = optionalPlayer.get();
        logger.info("Received login message for player {} on this proxy", player.getUsername());

        api.getPlayerManager().getPlayer(LoginMessage.getPlayerUniqueId(message)).thenAccept(matrixPlayer -> {
            if (matrixPlayer == null) {
                logger.error("Could not find matrix player for {}", player.getUsername());
                try {
                    context.acknowledge();
                } catch (IOException e) {
                    logger.error("Error acknowledging login message", e);
                }
                return;
            }

            String targetServerName = configuration.getString("login.target");
            Optional<RegisteredServer> target = server.getServer(targetServerName);
            if (target.isEmpty()) {
                error(player, "Could not find target server {} for {}", targetServerName, matrixPlayer.getName());
                try {
                    context.acknowledge();
                } catch (IOException e) {
                    logger.error("Error acknowledging login message", e);
                }
                return;
            }

            player.sendMessage(Component.text("Iniciando sesión...").color(TextColor.color(0x767676)));
            player.createConnectionRequest(target.get()).connect().thenAccept(result -> {
                if (!result.isSuccessful()) {
                    error(player, "Could not connect to target server {} for {}, status {}", targetServerName, player.getUsername(), result.getStatus());
                } else {
                    player.sendMessage(Component.text("¡Sesión iniciada correctamente!").color(TextColor.color(0x3FFF59)));
                }
                try {
                    context.acknowledge();
                } catch (IOException e) {
                    logger.error("Error acknowledging login message", e);
                }
            });
        });
    }

    private void error(Player player, String message, Object... params) {
        logger.error(message, params);
        player.disconnect(Component.newline().append(Component.text("Ha ocurrido un problema mientras intentábamos iniciar tu sesión, intenta más tarde")));
    }
}
