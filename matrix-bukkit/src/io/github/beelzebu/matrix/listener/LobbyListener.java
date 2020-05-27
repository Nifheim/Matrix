package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Beelzebu
 */
public class LobbyListener implements Listener {

    private static final Set<Player> editMode = new HashSet<>();
    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI api = Matrix.getAPI();
    private final Map<MatrixPlayer, Set<PlayerOptionType>> playerOptions = new HashMap<>();

    public LobbyListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    public static Set<Player> getEditMode() {
        return LobbyListener.editMode;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (editMode.contains(e.getPlayer())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (editMode.contains(e.getPlayer())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPvP(EntityDamageByEntityEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") == null ? e.getEntity().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getEntity().getWorld().getName()))) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") == null ? e.getEntity().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getEntity().getWorld().getName()))) {
            if (e.getCause().equals(DamageCause.FALL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRain(WeatherChangeEvent e) {
        if (e.toWeatherState()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        MatrixPlayer np = api.getPlayer(e.getPlayer().getUniqueId());
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()), 2);
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                int level = NetworkXP.getLevelForPlayer(p.getUniqueId());
                int xp = (int) (NetworkXP.MCEXP.getXPForPlayer(p.getUniqueId()) - NetworkXP.MCEXP.getXPForLevel(level));
                // Set the level and xp to 0 for security reasons
                p.setLevel(0);
                p.setExp(0);
                p.setLevel(level);
                p.giveExp(xp);
                p.setExhaustion(20);
                p.setSaturation(20);
                p.getActivePotionEffects().forEach(pe -> p.removePotionEffect(pe.getType()));
                p.getInventory().setHeldItemSlot(4);
            }, 2);
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
                p.setGameMode(GameMode.ADVENTURE);
                if (np.getOption(PlayerOptionType.SPEED)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, false, false));
                }
                if (np.getOption(PlayerOptionType.FLY)) {
                    p.setAllowFlight(true);
                }
            }, 10);
        } else {
            playerOptions.put(np, np.getOptions());
            np.getOptions().forEach(option -> np.setOption(option, false));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        MatrixPlayer np = api.getPlayer(p.getUniqueId());
        if (playerOptions.containsKey(np)) {
            playerOptions.get(np).forEach(option -> np.setOption(option, true));
            playerOptions.remove(np);
        }
        editMode.remove(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        MatrixPlayer p = api.getPlayer(e.getPlayer().getUniqueId());
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (playerOptions.containsKey(p)) {
                playerOptions.get(p).forEach(option -> p.setOption(option, true));
            }
        } else {
            playerOptions.put(p, p.getOptions());
            p.getOptions().forEach(option -> p.setOption(option, false));
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getFoodLevel() != 20) {
                e.setFoodLevel(20);
            }
            e.setCancelled(true);
        }
    }
}
