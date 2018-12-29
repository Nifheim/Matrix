package io.github.beelzebu.matrix.channels;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import io.github.beelzebu.matrix.utils.PermsUtils;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

@Data
public class Channel {

    private final String name;
    private final String permission;
    private final ChatColor color;

    public Channel register() {
        ProxyServer.getInstance().getPluginManager().registerCommand(ProxyServer.getInstance().getPluginManager().getPlugin("Matrix"), new Command(name) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                Matrix.getAPI().getPlugin().runAsync(() -> {
                    if (sender.hasPermission(permission)) {
                        if (args.length == 0 && sender instanceof ProxiedPlayer) {
                            Matrix.getAPI().getPlayer(sender.getName()).setStaffChannel(name);
                            return;
                        }
                        String name;
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer pp = (ProxiedPlayer) sender;
                            name = PermsUtils.getPrefix(pp.getUniqueId()) + Matrix.getAPI().getPlayer(pp.getUniqueId()).getDisplayName();
                        } else {
                            name = sender.getName();
                        }
                        StringBuilder msg = new StringBuilder(name);
                        msg.append("&f:&r ").append(color.toString());
                        for (String arg : args) {
                            msg.append(arg).append(" ");
                        }
                        msg.substring(0, msg.length() - 1);
                        new StaffChatMessage(permission, msg.toString()).send();
                    }
                });
            }
        });
        return this;
    }
}
