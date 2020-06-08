package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.menu.GUIManager;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        UUID inventoryUUID = GUIManager.getOpenInventories().get(p.getUniqueId());
        if (inventoryUUID != null) {
            e.setCancelled(true);
            GUIManager gui = GUIManager.getInventoriesByUUID().get(inventoryUUID);
            GUIManager.GUIAction action = gui.getActions().get(e.getSlot());
            if (action != null) {
                action.click(p);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        GUIManager.getOpenInventories().remove(playerUUID);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        GUIManager.getOpenInventories().remove(playerUUID);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType().equals(Material.AIR)) {
            return;
        }
        for (Map.Entry<UUID, GUIManager> entry : GUIManager.getInventoriesByUUID().entrySet()) {
            if (entry.getValue().getOpener() != null && e.getItem().getItemMeta().equals(entry.getValue().getOpener().getItemMeta())) {
                entry.getValue().open(e.getPlayer());
                break;
            }
        }
    }
}
