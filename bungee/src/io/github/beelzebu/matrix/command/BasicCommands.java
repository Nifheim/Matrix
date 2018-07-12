package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.api.CentredMessage;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class BasicCommands {

    private final MatrixAPI core = MatrixAPI.getInstance();
    private final Set<Command> commands = new HashSet<>();

    public BasicCommands(Main plugin) {
        createCommands();
        commands.forEach(cmd -> ProxyServer.getInstance().getPluginManager().registerCommand(plugin, cmd));
    }

    private void createCommands() {
        commands.add(new Command("twitter") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7puedes visitar nuestro Twitter desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&4https://twitter.com/vulthurmc")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("facebook", null, "fb") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7puedes visitar nuestro Facebook desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&4https://www.facebook.com/Vulthur")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("instagram", null, "insta") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(core.rep(CentredMessage.generate("&7puedes visitar nuestro Instagram desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&4https://www.instagram.com/minecraft_vulthur/")));
                sender.sendMessage("");
                sender.sendMessage(core.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        /*
        commands.add(new Command("pauth") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (sender instanceof ProxiedPlayer && args.length == 1) {
                    ProxiedPlayer pp = (ProxiedPlayer) sender;
                    if (!LoginListener.getAuthed().contains(pp.getUniqueId()) && args[0].equals(core.getConfig().getString(pp.getName() + ".Password"))) {
                        LoginListener.getAuthed().add(pp.getUniqueId());
                        core.getRedis().publish("core-auth", "connect:" + pp.getUniqueId());
                        pp.sendMessage(core.rep("&6Autentificado correctamente"));
                    }
                }
            }
            });*/
    }
}
