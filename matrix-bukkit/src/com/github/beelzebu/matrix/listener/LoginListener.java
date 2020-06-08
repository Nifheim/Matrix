package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.util.PermsUtils;
import com.github.beelzebu.matrix.util.ReadURL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class LoginListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI api;
    private boolean firstjoin = true;
    private final GameType gameType = Matrix.getAPI().getServerInfo().getGameType();
    private final Map<UUID, Long> playTime = new HashMap<>();

    public LoginListener(MatrixAPI api, MatrixBukkitBootstrap plugin) {
        this.api = api;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        MatrixPlayer matrixPlayer = api.getPlayer(player.getUniqueId());
        matrixPlayer.setLastGameType(gameType);
        matrixPlayer.addPlayedGame(gameType);
        playTime.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        ServerType type = api.getServerInfo().getServerType();
        if ((type.equals(ServerType.LOBBY) || type.equals(ServerType.SURVIVAL))) {
            if (!matrixPlayer.isVanished()) {
                if (player.hasPermission("matrix.joinmessage")) {
                    e.setJoinMessage(StringUtils.replace(" &8[&a+&8] &f" + PermsUtils.getPrefix(player.getUniqueId()) + api.getPlayer(player.getUniqueId()).getDisplayName() + " &ese ha unido al servidor"));
                }
                Bukkit.getOnlinePlayers().forEach(op -> op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 2));
            }
        }
        // Async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isVotifier()) {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + player.getName() + "&clave=" + plugin.getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(LoginListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", player.getName());
                }
            }
        });
        // Later task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (firstjoin) {
                plugin.getConfig().getStringList("Join cmds").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                firstjoin = false;
            }
            if (!player.hasPermission("matrix.command.fly")) {
                matrixPlayer.setOption(PlayerOptionType.FLY, false);
            }
        }, 6);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        matrixPlayer.setLastPlayTime(gameType, System.currentTimeMillis() - playTime.get(matrixPlayer.getUniqueId()));
        Matrix.getAPI().getPlayers().remove(matrixPlayer);
    }
}
