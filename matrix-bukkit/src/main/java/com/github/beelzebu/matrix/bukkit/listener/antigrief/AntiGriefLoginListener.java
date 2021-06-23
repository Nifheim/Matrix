package com.github.beelzebu.matrix.bukkit.listener.antigrief;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.beelzebu.matrix.util.ErrorCodes;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class AntiGriefLoginListener implements Listener {

    private final MatrixBukkitAPI api;
    private final Set<UUID> premiumPlayer = new HashSet<>();

    public AntiGriefLoginListener(MatrixBukkitAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(@NotNull AsyncPlayerPreLoginEvent e) {
        MatrixPlayer matrixPlayer = api.getPlayerManager().getPlayer(e.getUniqueId()).join();
        if (matrixPlayer == null) { // si el usuario aún no existe en la base de datos es porque no ha entrado por el proxy
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            api.getDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(1)", e.getName(), e.getAddress().getHostAddress()));
            return;
        }
        if (!matrixPlayer.isPremium() && !Objects.equals(matrixPlayer.getUniqueId(), UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getName()).getBytes()))) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Internal error: " + ErrorCodes.UUID_DONTMATCH.getId() + "\n\nYour UUID doesn't match with the UUID associated to your name in our database.\nThis login attempt was recorded for security reasons.");
            api.getDatabase().addFailedLogin(e.getUniqueId(), e.getName(), "error login uuid spigot");
            return;
        }
        if (!matrixPlayer.isLoggedIn() && !api.getServerInfo().getGroupName().equals(ServerInfoImpl.AUTH_GROUP)) {
            if (matrixPlayer.isPremium() && api.getServerInfo().getGroupName().equals(ServerInfoImpl.MAIN_LOBBY_GROUP)) {
                premiumPlayer.add(e.getUniqueId());
                return;
            }
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            api.getDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(2)", e.getName(), e.getAddress().getHostAddress()));
            return;
        }
        if (!api.getPlayerManager().getUniqueIdByName(e.getName()).join().equals(e.getUniqueId())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Tu UUID no coincide con la UUID que hay en nuestra base de datos\ntus datos fueron registrados por seguridad.");
            api.getDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(3)", e.getName(), e.getAddress().getHostAddress()));
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        if (player.isOp()) {
            player.setOp(false);
        }
        if (premiumPlayer.contains(player.getUniqueId())) {
            premiumPlayer.remove(player.getUniqueId());
            player.kickPlayer("Se ha detectado un acceso no autorizado.");
            api.getDatabase().addFailedLogin(player.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on PlayerJoinEvent", player.getName(), player.getAddress().getAddress().getHostAddress()));
        }
    }
}
