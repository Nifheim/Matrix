package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.nifheim.bukkit.util.command.MatrixCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Beelzebu
 */
public class FreezeCommand extends MatrixCommand {

    public static final Set<UUID> FROZEN_PLAYERS = new HashSet<>();

    public FreezeCommand() {
        super("freeze", "matrix.command.freeze", false);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String senderLocale = sender instanceof Player ? ((Player) sender).getLocale().substring(0, 2) : I18n.DEFAULT_LOCALE;
        if (args.length < 1) {
            sender.sendMessage(I18n.tl(Message.ESSENTIALS_FREEZE_USAGE, senderLocale));
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            api.getPlayerManager().getPlayer(target.getUniqueId()).thenAccept(targetMatrixPlayer -> {
                if (FROZEN_PLAYERS.contains(target.getUniqueId())) {
                    FROZEN_PLAYERS.remove(target.getUniqueId());
                    target.setWalkSpeed(0.2f);
                    target.setFlySpeed(0.1f);
                    target.removePotionEffect(PotionEffectType.JUMP);
                    target.removePotionEffect(PotionEffectType.SPEED);
                    target.removePotionEffect(PotionEffectType.SLOW);
                    target.removePotionEffect(PotionEffectType.BLINDNESS);
                    //target.setViewDistance(ViewDistanceListener.getViewDistance(target));
                    target.sendMessage(I18n.tl(Message.ESSENTIALS_UNFREEZE_TARGET, targetMatrixPlayer.getLastLocale()));
                    sender.sendMessage(I18n.tl(Message.ESSENTIALS_UNFREEZE_SENDER, senderLocale));
                } else {
                    FROZEN_PLAYERS.add(target.getUniqueId());
                    target.setWalkSpeed(0);
                    target.setFlySpeed(0);
                    target.setFlying(false);
                    target.setAllowFlight(false);
                    //target.setViewDistance(2);
                    target.setFallDistance(0);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999999, -5, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, -5, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999999, 100, false, false));
                    target.sendTitle(I18n.tl(Message.ESSENTIALS_FREEZE_TARGET_TITLE, targetMatrixPlayer.getLastLocale()), I18n.tl(Message.ESSENTIALS_FREEZE_TARGET_SUBTITLE, targetMatrixPlayer.getLastLocale()), 20, 200, 30);
                    target.sendMessage(I18n.tl(Message.ESSENTIALS_FREEZE_TARGET, targetMatrixPlayer.getLastLocale()));
                    if (args.length >= 2) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        sender.sendMessage(StringUtils.replace("%prefix% &6" + sb));
                    }
                    sender.sendMessage(I18n.tl(Message.ESSENTIALS_FREEZE_SENDER, senderLocale));
                }
            });
        } else {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_TARGET, senderLocale).replace("%player%", args[0]));
        }
    }
}
