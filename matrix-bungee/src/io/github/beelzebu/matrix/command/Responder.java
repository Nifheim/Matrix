package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Responder extends Command {

    private final MatrixAPI api = Matrix.getAPI();

    public Responder() {
        super("responder", "matrix.staff.helper");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 1 && api.getPlugin().isOnline(args[0], false)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length - 1; i++) {
                sb.append(args[i]).append(" ");
            }
            sb.append(args[args.length - 1]);
            TargetedMessage targetedMessage = new TargetedMessage(api.getPlayer(args[0]).getUniqueId(), (sender instanceof ProxiedPlayer ? api.getPlayer(((ProxiedPlayer) sender).getUniqueId()).getDisplayName() : "Consola") + "&f: " + sb.toString());
            api.getRedis().sendMessage(targetedMessage.getChannel(), api.getGson().toJson(targetedMessage));
            sender.sendMessage(api.rep("&6Haz enviado el siguiente mensaje a &7" + args[0]));
            sender.sendMessage(sb.toString());
        }
    }
}
