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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Channel)) {
            return false;
        }
        Channel other = (Channel) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        java.lang.Object this$name = name;
        java.lang.Object other$name = other.name;
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        java.lang.Object this$command = command;
        java.lang.Object other$command = other.command;
        if (this$command == null ? other$command != null : !this$command.equals(other$command)) {
            return false;
        }
        java.lang.Object this$permission = permission;
        java.lang.Object other$permission = other.permission;
        if (this$permission == null ? other$permission != null : !this$permission.equals(other$permission)) {
            return false;
        }
        java.lang.Object this$color = color;
        java.lang.Object other$color = other.color;
        if (this$color == null ? other$color != null : !this$color.equals(other$color)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        java.lang.Object $name = name;
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        java.lang.Object $command = command;
        result = result * PRIME + ($command == null ? 43 : $command.hashCode());
        java.lang.Object $permission = permission;
        result = result * PRIME + ($permission == null ? 43 : $permission.hashCode());
        java.lang.Object $color = color;
        result = result * PRIME + ($color == null ? 43 : $color.hashCode());
        return result;
    }

    public String toString() {
        return "Channel(name=" + name + ", command=" + command + ", permission=" + permission + ", color=" + color + ")";
    }

    protected boolean canEqual(Object other) {
        return other instanceof Channel;
    }
}
