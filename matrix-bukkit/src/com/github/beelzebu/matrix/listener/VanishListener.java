package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.command.staff.VanishCommand;
import com.github.beelzebu.matrix.util.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Beelzebu
 */
public class VanishListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public VanishListener(MatrixBukkitBootstrap plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                MatrixPlayer matrixPlayer = plugin.getApi().getPlayer(player.getUniqueId());
                for (Player player2 : Bukkit.getOnlinePlayers()) {
                    if (player.getUniqueId() == player2.getUniqueId()) {
                        continue;
                    }
                    if (matrixPlayer.isVanished() && player2.canSee(player)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_9)) {
                                player2.hidePlayer(plugin, player);
                            } else {
                                player2.hidePlayer(player);
                            }
                        });
                    }
                    if (!matrixPlayer.isVanished() && !player2.canSee(player)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_9)) {
                                player2.showPlayer(plugin, player);
                            }
                        });
                    }
                }
            }
        }, 0, 1);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        checkVanish(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        checkVanish(event.getPlayer());
    }

    @EventHandler
    public void onGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        MatrixPlayer matrixPlayer = plugin.getApi().getPlayer(player.getUniqueId());
        if (player.hasPermission(VanishCommand.PERMISSION)) {
            boolean toVanish = event.getNewGameMode() == GameMode.SPECTATOR;
            if (matrixPlayer.isVanished()) {
                if (player.getGameMode() != GameMode.SPECTATOR &&
                        event.getNewGameMode() != GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
                if (!toVanish) {
                    event.setCancelled(true);
                    return;
                }
            }
            matrixPlayer.setGameMode(cl.indiopikaro.jmatrix.api.player.GameMode.valueOf(event.getNewGameMode().toString()), plugin.getApi().getServerInfo().getGameType());
        }
    }

    private void checkVanish(Player player) {
        MatrixPlayer matrixPlayer = plugin.getApi().getPlayer(player.getUniqueId());
        if (matrixPlayer.isVanished()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(GameMode.valueOf(matrixPlayer.getGameMode(plugin.getApi().getServerInfo().getGameType()).toString()));
        }
    }
}
