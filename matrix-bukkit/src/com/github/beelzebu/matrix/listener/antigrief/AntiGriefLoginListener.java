package com.github.beelzebu.matrix.listener.antigrief;

import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Beelzebu
 */
public class AntiGriefLoginListener implements Listener {

    private final MatrixAPI api;
    private final Set<UUID> premiumPlayer = new HashSet<>();

    public AntiGriefLoginListener(MatrixAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        MatrixPlayer matrixPlayer = api.getPlayer(e.getUniqueId());
        if (matrixPlayer == null) { // si el usuario aún no existe en la base de datos es porque no ha entrado por el proxy
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            api.getSQLDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(1)", e.getName(), e.getAddress().getHostAddress()));
            return;
        }
        if (!matrixPlayer.isLoggedIn() && !api.getServerInfo().getGroupName().equals("auth")) {
            if (matrixPlayer.isPremium() && api.getServerInfo().getGroupName().equals("lobby")) {
                premiumPlayer.add(e.getUniqueId());
                return;
            }
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            api.getSQLDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(2)", e.getName(), e.getAddress().getHostAddress()));
            return;
        }
        if (!api.getPlayer(e.getName()).getUniqueId().equals(e.getUniqueId())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Tu UUID no coincide con la UUID que hay en nuestra base de datos\ntus datos fueron registrados por seguridad.");
            api.getSQLDatabase().addFailedLogin(e.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on AsyncPlayerPreLoginEvent(3)", e.getName(), e.getAddress().getHostAddress()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        if (player.isOp()) {
            player.setOp(false);
        }
        if (premiumPlayer.contains(player.getUniqueId())) {
            premiumPlayer.remove(player.getUniqueId());
            player.kickPlayer("Se ha detectado un acceso no autorizado.");
            api.getSQLDatabase().addFailedLogin(player.getUniqueId(), api.getServerInfo().getServerName(), String.format("name (%s) address (%s) Failed login on PlayerJoinEvent", player.getName(), player.getAddress().getAddress().getHostAddress()));
        }
    }
}
