package io.github.beelzebu.matrix.api.command;

import io.github.beelzebu.matrix.api.util.StringUtils;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
@RequiredArgsConstructor
public class BungeeCommandSource implements CommandSource {

    private final CommandSender sender;

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
