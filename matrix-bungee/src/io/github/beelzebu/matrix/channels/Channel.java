package io.github.beelzebu.matrix.channels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

@Getter
@AllArgsConstructor
public class Channel {

    private final String name;
    private final Command command;
    private final String permission;
    private final String color;

    public String getColor() {
        return (ChatColor.COLOR_CHAR + color);
    }

    public Channel register() {
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        ProxyServer.getInstance().getPluginManager().registerCommand(ProxyServer.getInstance().getPluginManager().getPlugin("Matrix"), command);
        return this;
    }
}
