package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import net.nifheim.bukkit.util.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final MatrixBukkitAPI api;

    public PlayerDeathListener(MatrixBukkitAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        int stay = api.getConfig().getInt("Death Titles.Stay", 60);
        Player player = e.getEntity();
        if (!CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
            try {
                Bukkit.getScheduler().runTaskLater(api.getPlugin().getBootstrap(), () -> player.spigot().respawn(), 1L);
            } catch (Exception ignore) { // Doesn't work in 1.8 or earlier
            }
        }
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(api.getPlugin().getBootstrap(), () -> {
            if (api.getConfig().getString("Death Titles.Mode", "Disabled").equalsIgnoreCase("disabled")) {
                return;
            }
            if (api.getConfig().getString("Death Titles.Mode").equalsIgnoreCase("pvp")) {
                if (player.getKiller() == null) {
                    return;
                }
            }
            player.sendTitle(I18n.tl(Message.DEATH_TITLES_TITLE, player.getLocale().substring(0, 2)), I18n.tl(Message.DEATH_TITLES_SUBTITLE, player.getLocale().substring(0, 2)), 30, stay, 30);
        }, 10L);
    }
}
