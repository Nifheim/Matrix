package com.github.beelzebu.matrix.api.command;

import cl.indiopikaro.jmatrix.api.command.CommandSource;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
public class BungeeCommandSource implements CommandSource {

    private final CommandSender sender;

    public BungeeCommandSource(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void execute(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(StringUtils.replace(message)));
    }
}
