package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.bungee.util.ServerUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class HubCommand extends Command {

    public HubCommand() {
        super("hub");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) commandSender;
            proxiedPlayer.connect(ServerUtil.getRandomLobby(), ServerConnectEvent.Reason.COMMAND);
        } else {
            commandSender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE)));
        }
    }
}
