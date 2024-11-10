package net.nifheim.matrix.api.command;

import net.nifheim.matrix.api.util.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class BungeeCommandSource implements CommandSource {

    private final CommandSender sender;

    public BungeeCommandSource(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public @NotNull String getName() {
        return sender.getName();
    }

    @Override
    public void execute(@NotNull String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', StringUtils.replace(message))));
    }
}
