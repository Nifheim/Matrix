package com.github.beelzebu.matrix.channels;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.util.PermsUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Channel {

    private final String name;
    private final String command;
    private final String permission;
    private final ChatColor color;

    public Channel(String name, String command, String permission, ChatColor color) {
        this.name = name;
        this.command = command;
        this.permission = permission;
        this.color = color;
    }

    public Channel register() {
        ProxyServer.getInstance().getPluginManager().registerCommand(ProxyServer.getInstance().getPluginManager().getPlugin("Matrix"), new Command(name) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                Matrix.getAPI().getPlugin().runAsync(() -> {
                    if (sender.hasPermission(permission)) {
                        if (args.length == 0 && sender instanceof ProxiedPlayer) {
                            if (Matrix.getAPI().getPlayer(sender.getName()).getStaffChannel() == null) {
                                Matrix.getAPI().getPlayer(sender.getName()).setStaffChannel(command);
                                sender.sendMessage(StringUtils.replace("&eTodos tus mensajes serán enviados a " + color + name));
                            } else {
                                Matrix.getAPI().getPlayer(sender.getName()).setStaffChannel(null);
                                sender.sendMessage(StringUtils.replace("&eTu chat vuelve a la normalidad."));
                            }
                            return;
                        }
                        String name;
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer pp = (ProxiedPlayer) sender;
                            name = PermsUtils.getPrefix(pp.getUniqueId()) + Matrix.getAPI().getPlayer(pp.getUniqueId()).getDisplayName();
                        } else {
                            name = sender.getName();
                        }
                        StringBuilder msg = new StringBuilder("&8[" + color + Channel.this.name.toUpperCase() + "&8]&r " + name);
                        msg.append("&f:&r ").append(color.toString());
                        for (String arg : args) {
                            msg.append(arg).append(" ");
                        }
                        msg.substring(0, msg.length() - 1);
                        msg.append("&r");
                        new StaffChatMessage(permission, msg.toString()).send();
                    }
                });
            }
        });
        return this;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }

    public ChatColor getColor() {
        return color;
    }

    public String toString() {
        return "Channel(name=" + name + ", command=" + command + ", permission=" + permission + ", color=" + color + ")";
    }

}
