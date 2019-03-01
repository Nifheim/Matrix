package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.command.staff.VanishCommand;
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

    public VanishListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(matrixBukkitBootstrap, () -> Bukkit.getOnlinePlayers().stream().map(p -> matrixBukkitBootstrap.getApi().getPlayer(p.getUniqueId())).filter(MatrixPlayer::isVanished).forEach(matrixPlayer -> Bukkit.getOnlinePlayers().stream().filter(op -> op.getUniqueId() != matrixPlayer.getUniqueId()).filter(op -> op.canSee(Bukkit.getPlayer(matrixPlayer.getUniqueId()))).forEach(op -> op.hidePlayer(matrixBukkitBootstrap, Bukkit.getPlayer(matrixPlayer.getUniqueId())))), 0, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkVanish(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        checkVanish(event.getPlayer());
    }

    @EventHandler
    public void onGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(player.getUniqueId());
        if (player.hasPermission(VanishCommand.PERMISSION)) {
            boolean toVanish = event.getNewGameMode() == GameMode.SPECTATOR;
            if (matrixPlayer.isVanished()) {
                if (!toVanish) {
                    event.setCancelled(true);
                }
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    private void checkVanish(Player player) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(player.getUniqueId());
        if (matrixPlayer.isVanished()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
}
