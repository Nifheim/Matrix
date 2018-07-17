package io.github.beelzebu.matrix.command;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HelpOP extends Command {

    private final MatrixAPI api = Matrix.getAPI();
    private final Map<UUID, Long> timer = new HashMap<>();

    public HelpOP() {
        super("helpop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        api.getPlugin().runAsync(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (String arg : args) {
                stringBuilder.append(arg).append(" ");
            }
            if (sender instanceof ProxiedPlayer) {
                if (args.length >= 1 && args[0].length() >= 2) {
                    if (!timer.containsKey(((ProxiedPlayer) sender).getUniqueId()) || timer.get(((ProxiedPlayer) sender).getUniqueId()) <= System.currentTimeMillis()) {
                        RedisBungee.getApi().sendChannelMessage("NifheimHelpop", "§4§l[Ayuda] §8[§a§o" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "§8] §c" + api.getPlayer(sender.getName()).getDisplayname() + "§f: §e" + stringBuilder.toString());
                        sender.sendMessage(TextComponent.fromLegacyText("§a§lNifheim §8§l> §7El siguiente mensaje fue enviado a todos los staff online, si alguno está disponible serás ayudado en la inmediatez, por favor no hagas spam del comando o podrás ser sancionad@."));
                        sender.sendMessage("§4§l[Ayuda] §8[§a§o" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "§8] §c" + api.getPlayer(sender.getName()).getDisplayname() + "§f: §e" + stringBuilder.toString());
                        if (!sender.hasPermission("matrix.staff")) {
                            timer.put(((ProxiedPlayer) sender).getUniqueId(), System.currentTimeMillis() + 30000);
                        }
                    } else if (timer.containsKey(((ProxiedPlayer) sender).getUniqueId()) && timer.get(((ProxiedPlayer) sender).getUniqueId()) > System.currentTimeMillis()) {
                        sender.sendMessage("§a§lNifheim §8§l> §7Debes esperar " + (timer.get(((ProxiedPlayer) sender).getUniqueId()) - System.currentTimeMillis()) / 1000 + " segundos más para usar este comando.");
                    }
                } else {
                    sender.sendMessage("§a§lNifheim §8§l> §7Por favor escribe un mensaje válido.");
                }
            }
        });
    }
}
