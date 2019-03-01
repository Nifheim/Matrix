package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.util.PermsUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCommand extends Command {

    private final MatrixBungeeBootstrap bootstrap;

    public ReplyCommand(MatrixBungeeBootstrap bootstrap) {
        super("responder", "matrix.helper");
        this.bootstrap = bootstrap;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(StringUtils.replace("&cPor favor ingresa un usuario y mensaje para enviar."));
            return;
        }
        MatrixPlayer target = bootstrap.getApi().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(StringUtils.replace("&cEl usuario al que intentas responder no existe."));
            return;
        }
        if (bootstrap.getApi().getPlugin().isOnline(target.getUniqueId(), false)) {
            String name;
            if (sender instanceof ProxiedPlayer) {
                if (target.getUniqueId() == ((ProxiedPlayer) sender).getUniqueId()) {
                    sender.sendMessage(StringUtils.replace("&cNo te puedes responder a ti mismo."));
                    return;
                }
                name = PermsUtils.getPrefix(((ProxiedPlayer) sender).getUniqueId()) + bootstrap.getApi().getPlayer(((ProxiedPlayer) sender).getUniqueId()).getDisplayName();
            } else {
                name = sender.getName();
            }
            StringBuilder message = new StringBuilder(name);
            message.append("&f: ");
            for (int i = 1; i < args.length - 1; i++) {
                message.append(args[i]).append(" ");
            }
            message.append(args[args.length - 1]);
            message = new StringBuilder(StringUtils.replace(message.toString()));
            new TargetedMessage(target.getUniqueId(), message.toString()).send();
            sender.sendMessage(StringUtils.replace("&6Haz enviado el siguiente mensaje a &7" + target.getDisplayName()));
            sender.sendMessage(message.toString());
        } else {
            sender.sendMessage(StringUtils.replace("&cEl usuario al que intentas responder no está online."));
        }
    }
}
