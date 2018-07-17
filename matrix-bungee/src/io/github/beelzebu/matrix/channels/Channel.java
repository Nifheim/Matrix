package io.github.beelzebu.matrix.channels;

import io.github.beelzebu.matrix.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class Channel {

    private final String n;
    private final Command c;
    private final String p;
    private final String co;

    public Channel(String name, Command command, String permission, String color) {
        n = name;
        c = command;
        p = permission;
        co = color;
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        ProxyServer.getInstance().getPluginManager().registerCommand(Main.getInstance(), command);
    }

    /**
     * @return The name of the channel.
     */
    public String getName() {
        return n;
    }

    /**
     * @return The command of the channel.
     */
    public Command getCommand() {
        return c;
    }

    /**
     * @return The permission of the channel.
     */
    public String getPermission() {
        return p;
    }

    public String getColor() {
        return ("§" + co);
    }
}
