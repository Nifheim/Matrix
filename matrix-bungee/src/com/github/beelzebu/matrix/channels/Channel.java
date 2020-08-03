package com.github.beelzebu.matrix.channels;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.messaging.message.StaffChatMessage;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.util.PermsUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class Channel {

    private final String name;
    private final String command;
    private final String permission;
    private final ChatColor color;
    private Command bungeeCommand;

    public Channel(String name, String command, String permission, ChatColor color) {
        this.name = name;
        this.command = command.toLowerCase();
        this.permission = permission;
        this.color = color;
    }

    public Channel register() {
        ProxyServer.getInstance().getPluginManager().registerCommand(ProxyServer.getInstance().getPluginManager().getPlugin("Matrix"), bungeeCommand = new Command(name) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (!sender.hasPermission(permission)) {
                    return;
                }
                Matrix.getAPI().getPlugin().runAsync(() -> {
                    String locale = I18n.DEFAULT_LOCALE;
                    if (sender instanceof ProxiedPlayer) {
                        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
                        locale = matrixPlayer.getLastLocale();
                        if (args.length == 0) {
                            if (matrixPlayer.getStaffChannel() == null) {
                                matrixPlayer.setStaffChannel(command);
                                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.CHANNEL_JOIN, locale).replace("%channel%", name)));
                            } else {
                                Matrix.getAPI().getPlayer(sender.getName()).setStaffChannel(null);
                                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.CHANNEL_LEAVE, locale)));
                            }
                            return;
                        }
                    }
                    String channel = I18n.tl(Message.CHANNEL_MESSAGE_CHANNEL, locale);
                    String prefix = I18n.tl(Message.CHANNEL_MESSAGE_PREFIX, locale);
                    String name = I18n.tl(Message.CHANNEL_MESSAGE_NAME, locale);
                    String suffix = I18n.tl(Message.CHANNEL_MESSAGE_SUFFIX, locale);
                    String message = I18n.tl(Message.CHANNEL_MESSAGE_MESSAGE, locale);
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
                        channel = setPlaceholders(channel, proxiedPlayer, Channel.this);
                        prefix = setPlaceholders(prefix, proxiedPlayer, Channel.this);
                        name = setPlaceholders(name, proxiedPlayer, Channel.this);
                    } else {
                        prefix = prefix.replace("%player_prefix%", "");
                        name = name.replace("%player_name%", sender.getName());
                    }
                    suffix = suffix.replace("%player_suffix%", "");
                    StringBuilder msg = new StringBuilder();
                    for (String arg : args) {
                        msg.append(arg).append(" ");
                    }
                    msg.substring(0, msg.length() - 1);
                    msg.append("&r");
                    message = message.replace("%message%", msg);
                    new StaffChatMessage(permission, channel + prefix + name + suffix + color + message).send();
                });
            }
        });
        return this;
    }

    public void unregister() {
        if (bungeeCommand != null) {
            ProxyServer.getInstance().getPluginManager().unregisterCommand(bungeeCommand);
        }
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

    private String setPlaceholders(String string, ProxiedPlayer proxiedPlayer, Channel channel) {
        return string.replace("%channel%", channel.getName().toUpperCase())
                .replace("%player_name%", proxiedPlayer.getName())
                .replace("%player_prefix%", PermsUtils.getPrefix(proxiedPlayer.getUniqueId()))
                .replace("%player_suffix%", "")
                .replace("%server_name%", proxiedPlayer.getServer().getInfo().getName());
    }
}
