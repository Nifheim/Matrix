package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.server.GameType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class LoginListener implements Listener {

    private final GameType gameType = Matrix.getAPI().getServerInfo().getGameType();
    private final Map<UUID, Long> playTime = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        matrixPlayer.setLastGameType(gameType);
        matrixPlayer.addPlayedGame(gameType);
        playTime.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        //Matrix.getAPI().getPlayers().add(matrixPlayer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        matrixPlayer.setLastPlayTime(gameType, System.currentTimeMillis() - playTime.get(matrixPlayer.getUniqueId()));
        Matrix.getAPI().getPlayers().remove(matrixPlayer);
    }
}
