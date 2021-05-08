package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
import com.github.beelzebu.matrix.api.util.StringUtils;
import net.nifheim.bukkit.util.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand extends MatrixCommand {

    public ReplyCommand() {
        super("responder", "matrix.command.reply", "reply");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(StringUtils.replace("&cPor favor ingresa un usuario y mensaje para enviar."));
            return;
        }
        api.getPlayerManager().getPlayerByName(args[0]).thenAccept(target -> {
            if (target == null) {
                sender.sendMessage(StringUtils.replace("&cEl usuario al que intentas responder no existe."));
                return;
            }
            if (api.getPlugin().isOnline(target.getUniqueId(), false)) {
                String name;
                if (sender instanceof Player) {
                    if (target.getUniqueId() == ((Player) sender).getUniqueId()) {
                        sender.sendMessage(StringUtils.replace("&cNo te puedes responder a ti mismo."));
                        return;
                    }
                    name = api.getPlayerManager().getPlayer((Player) sender).join().getDisplayName();
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
        });
    }
}
