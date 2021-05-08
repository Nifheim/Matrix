package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.bukkit.command.staff.VanishCommand;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class VanishListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public VanishListener(MatrixBukkitBootstrap plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        checkVanish(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerTeleportEvent e) {
        checkVanish(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        checkVanish(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChangeEvent(@NotNull PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        plugin.getApi().getPlayerManager().getPlayer(player).thenAccept(matrixPlayer -> {
            if (player.hasPermission(VanishCommand.PERMISSION) || matrixPlayer.isVanished()) {
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
                matrixPlayer.setGameMode(com.github.beelzebu.matrix.api.player.GameMode.valueOf(event.getNewGameMode().toString()), plugin.getApi().getServerInfo().getGroupName());
            }
        });
    }

    private void checkVanish(@NotNull Player player) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        plugin.getApi().getPlayerManager().getPlayer(player).thenAccept(matrixPlayer -> {
            if (matrixPlayer == null) {
                Matrix.getLogger().info("Null matrix player for: " + player.getName());
                return;
            }
            if (matrixPlayer.isVanished()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        });
    }
}
