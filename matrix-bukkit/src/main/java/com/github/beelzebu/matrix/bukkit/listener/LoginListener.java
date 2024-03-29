package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.util.MetaInjector;
import com.github.beelzebu.matrix.util.ReadURL;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nifheim.bukkit.util.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class LoginListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final @NotNull MatrixBukkitAPI api;
    private final @NotNull ServerInfo serverInfo;
    private boolean firstJoin = true;

    public LoginListener(@NotNull MatrixBukkitAPI api, MatrixBukkitBootstrap plugin) {
        this.api = api;
        serverInfo = api.getServerInfo();
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        api.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(matrixPlayer -> {
            matrixPlayer.setLastServerName(serverInfo.getServerName());
            matrixPlayer.setLastServerGroup(serverInfo.getGroupName());
            if ((serverInfo.getServerType().equals(ServerType.LOBBY) || serverInfo.getServerType().equals(ServerType.SURVIVAL) || serverInfo.getServerType().equals(ServerType.MINIGAME_MULTIARENA))) {
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
                    Bukkit.getOnlinePlayers().forEach(op -> op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 2));
                }
            }
            api.getPlayerManager().getMetaInjector().setMeta(player, MetaInjector.ID_KEY, matrixPlayer.getId());
        });
        // Async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + player.getName() + "&clave=" + plugin.getConfig().getString("clave"));
            } catch (Exception ex) {
                Logger.getLogger(LoginListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", player.getName());
            }
        });
        // Later task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (firstJoin) {
                plugin.getConfig().getStringList("Join cmds").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                firstJoin = false;
            }
            if (!player.hasPermission("matrix.command.fly")) {
                api.getPlayerManager().getPlayer(player).thenAccept(matrixPlayer -> matrixPlayer.setOption(PlayerOptionType.FLY, false));
            }
        }, 6);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}
