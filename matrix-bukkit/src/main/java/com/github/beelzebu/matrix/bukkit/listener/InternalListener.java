package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.bukkit.command.staff.FreezeCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class InternalListener implements Listener {

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
