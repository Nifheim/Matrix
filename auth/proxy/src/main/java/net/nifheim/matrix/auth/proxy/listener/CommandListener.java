package net.nifheim.matrix.auth.proxy.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Collections;
import java.util.Set;
import net.nifheim.matrix.auth.proxy.config.ConfigurationFile;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

public class CommandListener extends MatrixPlayerListener {

    private final Logger logger;
    private final Set<String> guestCommands;

    public CommandListener(Logger logger, ConfigurationFile configuration) {
        this.logger = logger;
        Set<String> configCommands;
        try {
            configCommands = Set.copyOf(configuration.getStringList("guest.allowed-commands"));
        } catch (SerializationException e) {
            logger.error("Failed to load guest commands from configuration file", e);
            configCommands = Collections.emptySet();
        }
        this.guestCommands = configCommands;
        logger.info("Loaded guest commands: {}", guestCommands.size());
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerCommandSend(PlayerAvailableCommandsEvent event) {
        // remove commands not present on guestCommands from brigadier's root command node
        return wrapTask(executePlayerLogic(event.getPlayer().getUniqueId(), matrixPlayer -> {
            if (matrixPlayer == null) {
                return;
            }
            if (matrixPlayer.isLoggedIn()) {
                return;
            }
            event.getRootNode().getChildren().removeIf(node -> !guestCommands.contains(node.getName()));
        }));
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerCommand(CommandExecuteEvent event) {
        if (event.getCommandSource() instanceof Player player) {
            return wrapTask(executePlayerLogic(player.identity().uuid(), matrixPlayer -> {
                if (matrixPlayer == null) {
                    event.setResult(CommandExecuteEvent.CommandResult.denied());
                    return;
                }
                if (matrixPlayer.isLoggedIn()) {
                    return;
                }
                if (!guestCommands.contains(event.getCommand())) {
                    event.setResult(CommandExecuteEvent.CommandResult.denied());
                }
            }));
        }
        return EventTask.withContinuation(Continuation::resume);
    }
}
