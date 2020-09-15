package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class BungeeTPCommand extends Command {

    public BungeeTPCommand() {
        super("btp", "matrix.command.btp", "bungeetp");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ProxiedPlayer) {
            if (args.length == 1) {
                ProxiedPlayer proxiedPlayer = (ProxiedPlayer) commandSender;
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[0]);
                if (serverInfo != null) {
                    proxiedPlayer.connect(serverInfo);
                } else {
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&cThere is no server with the specified name.")));
                }
            }
        } else {
            commandSender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE)));
        }
    }
}
