package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.api.Matrix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class PluginsCommand extends Command {

    public PluginsCommand() {
        super("bplugins", "matrix.staff.admin", "bpl");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            String plugins = "§fPlugins (" + ProxyServer.getInstance().getPluginManager().getPlugins().size() + "): ";
            plugins = ProxyServer.getInstance().getPluginManager().getPlugins().stream().map((plugin) -> "§a" + plugin.getDescription().getName() + "§f, ").reduce(plugins, String::concat);
            sender.sendMessage(TextComponent.fromLegacyText(plugins.substring(0, plugins.length() - 2)));
        } else if (args[0].equalsIgnoreCase("reload")) {
            Matrix.getAPI().getConfig().reload();
        }
    }
}
