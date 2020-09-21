package com.github.beelzebu.matrix.bukkit.command.utils;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.messaging.message.CommandMessage;
import java.util.Arrays;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class SyncCommand extends MatrixCommand {

    public SyncCommand() {
        super("sync", "matrix.owner");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String server = args[0];
        boolean global = args[1].equalsIgnoreCase("global");
        boolean bungee = args[1].equalsIgnoreCase("bungee");
        boolean bukkit = args[1].equalsIgnoreCase("bukkit");
        StringBuilder sb = new StringBuilder();
        for (String arg : Arrays.copyOfRange(args, 1, args.length)) {
            sb.append(arg).append(" ");
        }
        String command = sb.substring(0, sb.length() - 1);
        new CommandMessage(server, command, global, bungee, bukkit).send();
    }
}
