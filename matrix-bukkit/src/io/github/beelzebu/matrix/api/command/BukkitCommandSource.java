package io.github.beelzebu.matrix.api.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
@RequiredArgsConstructor
public class BukkitCommandSource implements CommandSource {

    private final CommandSender sender;

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void execute(String command) {
        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
}
