package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HelpOpCommand extends Command {

    private final MatrixBungeeBootstrap bootstrap;
    private final Map<UUID, Long> timer = new HashMap<>();

    public HelpOpCommand(MatrixBungeeBootstrap bootstrap) {
        super("helpop");
        this.bootstrap = bootstrap;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        bootstrap.getApi().getPlugin().runAsync(() -> {
            if (!(sender instanceof ProxiedPlayer)) {
                return;
            }
            if (args.length == 1 && args[0].length() <= 2) {
                sender.sendMessage("§a§lNifheim §8§l> §7Por favor escribe un mensaje válido.");
                return;
            }
            ProxiedPlayer pp = (ProxiedPlayer) sender;
            UUID uniqueId = pp.getUniqueId();
            if (timer.containsKey(uniqueId) && timer.get(uniqueId) > System.currentTimeMillis()) {
                sender.sendMessage("§a§lNifheim §8§l> §7Debes esperar " + (timer.get(uniqueId) - System.currentTimeMillis()) / 1000 + " segundos más para usar este comando.");
                return;
            }
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            if (!timer.containsKey(uniqueId) || timer.get(uniqueId) <= System.currentTimeMillis()) {
                MatrixPlayer matrixPlayer = bootstrap.getApi().getPlayer(uniqueId);
                new StaffChatMessage("matrix.helper", "§4§l[Ayuda] §8[§a§o" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "§8] §c" + matrixPlayer.getDisplayName() + "§f: §e" + message.toString()).send();
                sender.sendMessage("§4§l[Ayuda] §8[§a§o" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "§8] §c" + matrixPlayer.getDisplayName() + "§f: §e" + message.toString());
                if (!sender.hasPermission("matrix.helper")) {
                    timer.put(uniqueId, System.currentTimeMillis() + 30000);
                }
            }
        });
    }
}
