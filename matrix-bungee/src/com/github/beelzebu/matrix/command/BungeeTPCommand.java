package com.github.beelzebu.matrix.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
                ProxiedPlayer pp = (ProxiedPlayer) commandSender;
                pp.connect(ProxyServer.getInstance().getServerInfo(args[0]));
            }
        }
    }
}
