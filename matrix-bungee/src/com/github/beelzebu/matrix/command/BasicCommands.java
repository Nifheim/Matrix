package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.CentredMessage;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class BasicCommands {

    // TODO: configurable
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
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7puedes visitar nuestro Twitter desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&4https://twitter.com/NifheimNetwork")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("facebook", null, "fb") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7puedes visitar nuestro Facebook desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&4https://www.facebook.com/NifheimNetwork")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("instagram", null, "insta") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7puedes visitar nuestro Instagram desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&4https://www.instagram.com/NifheimNetwork/")));
                sender.sendMessage("");
                sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("discord") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (args.length == 0) {
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7Nos alegra que te intereses en nuestro discord")));
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7puedes unirte con el siguiente enlace:")));
                    sender.sendMessage("");
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&4https://www.nifheim.net/discord")));
                    sender.sendMessage("");
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&7Para obtener un código y verificar tu cuenta usa:")));
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&a/discord verify")));
                    sender.sendMessage("");
                    sender.sendMessage(StringUtils.replace(CentredMessage.generate("&8&m----------------------------------------")));
                } else if (args.length == 1 && args[0].equalsIgnoreCase("verify")) {
                    String key = "discord:";
                    String random;
                    try (Jedis jedis = api.getMessaging().getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
                        random = randomAlphaNumeric(6);
                        pipeline.expire(key + random, 360).setDependency(pipeline.set(key + random, sender.getName()));
                        pipeline.sync();
                        sender.sendMessage(StringUtils.replace("&fTu código de verificación es: &a" + random));
                        sender.sendMessage(StringUtils.replace("&fEl código expira en &a5&f minutos."));
                    }
                }
            }
        });
    }
}
