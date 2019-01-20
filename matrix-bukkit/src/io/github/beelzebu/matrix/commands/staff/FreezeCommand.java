package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.Titles;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.listeners.ViewDistanceListener;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FreezeCommand extends MatrixCommand {

    public static final Set<Player> FROZEN_PLAYERS = new HashSet<>();

    public FreezeCommand() {
        super("freeze", "matrix.mod");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(api.getString("Essentials.FreezeCommand.Usage", sender instanceof Player ? ((Player) sender).getLocale() : ""));
            return;
        }
        api.getPlugin().runSync(() -> {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (FROZEN_PLAYERS.contains(target)) {
                    FROZEN_PLAYERS.remove(target);
                    target.setWalkSpeed(0.2f);
                    target.setFlySpeed(0.1f);
                    target.removePotionEffect(PotionEffectType.JUMP);
                    target.setViewDistance(ViewDistanceListener.getViewDistance(target));
                    target.sendMessage(api.rep("%prefix% &fFuiste descongelado, puedes volver a jugar :)"));
                    sender.sendMessage(api.rep("%prefix% &aUsuario descongelado."));
                } else {
                    FROZEN_PLAYERS.add(target);
                    target.setWalkSpeed(0);
                    target.setFlySpeed(0);
                    target.setFlying(false);
                    target.setAllowFlight(false);
                    target.setViewDistance(2);
                    target.setFallDistance(0);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999999, -5, false, false));
                    Titles.sendTitle(target, 20, 200, 30, api.rep("&6NO TE DESCONECTES"), "");
                    target.sendMessage(api.getString("Essentials.FreezeCommand.Target", target.getLocale()));
                    if (args.length >= 2) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        sender.sendMessage(api.rep("%prefix% &6" + sb.toString()));
                    }
                    sender.sendMessage(api.rep("%prefix% &aUsuario congelado."));
                }
            } else {
                sender.sendMessage(api.rep("%prefix% &cEste jugador no está conectado."));
            }
        });
    }
}
