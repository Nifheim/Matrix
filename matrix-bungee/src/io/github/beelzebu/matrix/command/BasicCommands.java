package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.CentredMessage;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BasicCommands {

    private final MatrixAPI api = Matrix.getAPI();
    private final Set<Command> commands = new HashSet<>();

    public BasicCommands(MatrixBungeeBootstrap plugin) {
        createCommands();
        commands.forEach(cmd -> ProxyServer.getInstance().getPluginManager().registerCommand(plugin, cmd));
    }

    private void createCommands() {
        commands.add(new Command("twitter") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7puedes visitar nuestro Twitter desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&4https://twitter.com/NifheimNetwork")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("facebook", null, "fb") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7puedes visitar nuestro Facebook desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&4https://www.facebook.com/NifheimNetwork")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("instagram", null, "insta") {
            @Override
            public void execute(CommandSender sender, String[] arg1) {
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7Nos alegra que te intereses en nuestras redes sociales")));
                sender.sendMessage(api.rep(CentredMessage.generate("&7puedes visitar nuestro Instagram desde el siguiente enlace:")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&4https://www.instagram.com/NifheimNetwork/")));
                sender.sendMessage("");
                sender.sendMessage(api.rep(CentredMessage.generate("&8&m----------------------------------------")));
            }
        });
        commands.add(new Command("pauth") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (sender instanceof ProxiedPlayer && args.length == 1) {
                    ProxiedPlayer pp = (ProxiedPlayer) sender;
                    MatrixPlayer player = api.getPlayer(((ProxiedPlayer) sender).getUniqueId());
                    if (!player.isAuthed() && Objects.equals(args[0], player.getSecret())) {
                        player.setAuthed(true);
                        pp.sendMessage(new TextComponent("Logged in."));
                    }
                }
            }
        });
    }
}
