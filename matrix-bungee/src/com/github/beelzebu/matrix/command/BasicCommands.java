package com.github.beelzebu.matrix.command;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.CentredMessage;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class BasicCommands {

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final MatrixAPI api = Matrix.getAPI();
    private final Set<Command> commands = new HashSet<>();

    public BasicCommands(MatrixBungeeBootstrap plugin) {
        createCommands();
        commands.forEach(cmd -> ProxyServer.getInstance().getPluginManager().registerCommand(plugin, cmd));
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private void createCommands() {
        commands.add(new Command("twitter") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                if (sender instanceof ProxiedPlayer) {
                    MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
                    for (String line : I18n.tls(Message.COMMAND_SOCIAL_TWITTER, matrixPlayer.getLastLocale())) {
                        sender.sendMessage(TextComponent.fromLegacyText(CentredMessage.generate(line)));
                    }
                }
            }
        });
        commands.add(new Command("facebook", null, "fb") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                if (sender instanceof ProxiedPlayer) {
                    MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
                    for (String line : I18n.tls(Message.COMMAND_SOCIAL_FACEBOOK, matrixPlayer.getLastLocale())) {
                        sender.sendMessage(TextComponent.fromLegacyText(CentredMessage.generate(line)));
                    }
                }
            }
        });
        commands.add(new Command("instagram", null, "insta", "ig") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                if (sender instanceof ProxiedPlayer) {
                    MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
                    for (String line : I18n.tls(Message.COMMAND_SOCIAL_INSTAGRAM, matrixPlayer.getLastLocale())) {
                        sender.sendMessage(TextComponent.fromLegacyText(CentredMessage.generate(line)));
                    }
                }
            }
        });
        commands.add(new Command("discord") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (args.length == 0) {
                    if (sender instanceof ProxiedPlayer) {
                        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
                        for (String line : I18n.tls(Message.COMMAND_SOCIAL_DISCORD, matrixPlayer.getLastLocale())) {
                            sender.sendMessage(TextComponent.fromLegacyText(CentredMessage.generate(line)));
                        }
                    }
                } else if (args.length == 1 && args[0].equalsIgnoreCase("verify")) {
                    String random = randomAlphaNumeric(6);
                    api.getCache().setDiscordVerificationCode(sender.getName(), random);
                    sender.sendMessage(StringUtils.replace("&fTu código de verificación es: &a" + random));
                    sender.sendMessage(StringUtils.replace("&fEl código expira en &a5&f minutos."));
                }
            }
        });
    }
}
