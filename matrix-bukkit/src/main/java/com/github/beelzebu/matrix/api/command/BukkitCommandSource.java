package com.github.beelzebu.matrix.api.command;

import com.github.beelzebu.matrix.api.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class BukkitCommandSource implements CommandSource {

    private final CommandSender sender;

    public BukkitCommandSource(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public @NotNull String getName() {
        return sender.getName();
    }

    @Override
    public void execute(@NotNull String command) {
        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        sender.sendMessage(StringUtils.replace(message));
    }
}
