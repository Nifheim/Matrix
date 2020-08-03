package com.github.beelzebu.matrix.listener.lobby;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.server.ServerType;
import com.github.beelzebu.matrix.menus.OptionsGUI;
import com.github.beelzebu.matrix.util.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ItemListener implements Listener {

    private final MatrixAPI api = Matrix.getAPI();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() == Material.AIR) {
            return;
        }
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA) && (api.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName())))) {
            Player p = e.getPlayer();
            if (e.getItem().getType().equals(Material.COMPASS)) {
                //Bukkit.dispatchCommand(p, "servidores");
                p.chat("/servidores");
            } else if (e.getItem().getType().equals(CompatUtil.getInstance().getRedstoneComparator())) {
                new OptionsGUI(api.getPlayer(p.getUniqueId())).open(p);
            } else if (e.getItem().getType().equals(CompatUtil.getInstance().getPlayerHeadItem().getType())) {
                Bukkit.dispatchCommand(p, "perfil");
            }
        }
    }

    @EventHandler
    public void inventoryMoveEvent(InventoryMoveItemEvent e) {
        if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA) && (api.getConfig().getString("Lobby World") == null ? e.getWhoClicked().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getWhoClicked().getWorld().getName())))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDropEvent(PlayerDropItemEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA) && e.getPlayer().getWorld().getName().equals(api.getConfig().getString("Lobby World")))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemChange(PlayerSwapHandItemsEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA) && e.getPlayer().getWorld().getName().equals(api.getConfig().getString("Lobby World")))) {
            e.setCancelled(true);
        }
    }
}
