package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.CentredMessage;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.command.staff.FreezeCommand;
import io.github.beelzebu.matrix.event.LevelUPEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class InternalListener implements Listener {

    private final MatrixAPI core = Matrix.getAPI();

    @EventHandler
    public void onLevelUP(LevelUPEvent e) {
        Player p = e.getPlayer();
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        Bukkit.getScheduler().runTaskLaterAsynchronously(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class), () -> core.getMessages(p.getLocale()).getStringList("NetworkXP.LevelUP").forEach(line -> p.sendMessage(CentredMessage.generate(line.replaceAll("%player%", p.getName()).replaceAll("%level%", String.valueOf(e.getNewLvl())).replaceAll("%next%", String.valueOf(e.getXPForNextLvl()))))), 10);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (FreezeCommand.FROZEN_PLAYERS.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (FreezeCommand.FROZEN_PLAYERS.contains((Player) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }
}
