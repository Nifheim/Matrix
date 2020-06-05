package com.github.beelzebu.matrix.listener.lobby;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.ItemBuilder;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.util.CompatUtil;
import com.github.beelzebu.matrix.util.LocationUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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
                if (((Player) e.getDamager()).getInventory().getItem(EquipmentSlot.HEAD).getType() == Material.DIAMOND_HELMET && ((Player) e.getDamager()).getInventory().getItem(EquipmentSlot.HEAD).getType() == Material.DIAMOND_HELMET) {
                    return;
                }
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
        MatrixPlayer matrixPlayer = api.getPlayer(e.getPlayer().getUniqueId());
        Player player = e.getPlayer();
        setNormalItems(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(LocationUtils.locationFromString(LobbyData.getInstance().getConfig().getString("spawn", LocationUtils.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation())))), 2);
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                // Set the level and xp to 0 for security reasons
                player.setLevel(0);
                player.setExp(0);
                player.setExhaustion(20);
                player.setSaturation(20);
                player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
                player.getInventory().setHeldItemSlot(4);
            }, 2);
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
                player.setGameMode(GameMode.ADVENTURE);
                if (matrixPlayer.getOption(PlayerOptionType.SPEED)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, false, false));
                }
                if (matrixPlayer.getOption(PlayerOptionType.FLY)) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }, 10);
        } else {
            playerOptions.put(matrixPlayer, matrixPlayer.getOptions());
            matrixPlayer.getOptions().forEach(option -> matrixPlayer.setOption(option, false));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        MatrixPlayer matrixPlayer = api.getPlayer(player.getUniqueId());
        if (playerOptions.containsKey(matrixPlayer)) {
            playerOptions.get(matrixPlayer).forEach(option -> matrixPlayer.setOption(option, true));
            playerOptions.remove(matrixPlayer);
        }
        editMode.remove(player);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlyChange(PlayerToggleFlightEvent e) {
        if (e.isCancelled()) {
            return;
        }
        api.getPlayer(e.getPlayer().getUniqueId()).setOption(PlayerOptionType.FLY, e.getPlayer().getAllowFlight());
    }

    private void setNormalItems(Player p) {
        p.getInventory().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.LEGS, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.FEET, new ItemStack(Material.AIR));
        {
            ItemStack is = new ItemBuilder(Material.COMPASS, 1, api.getString("Lobby items.Server selector.Name", p.getLocale())).build();
            p.getInventory().setItem(0, is);
        }
        {
            ItemStack is = new ItemBuilder(CompatUtil.getInstance().getRedstoneComparator(), 1, api.getString("Lobby items.Options.Name", p.getLocale())).build();
            p.getInventory().setItem(1, is);
        }
        {
            ItemStack is = new ItemBuilder(CompatUtil.getInstance().getPlayerHeadItem()).amount(1).displayname(api.getString("Lobby items.Profile.Name", p.getLocale())).build();
            SkullMeta meta = (SkullMeta) is.getItemMeta();
            meta.setOwningPlayer(p);
            is.setItemMeta(meta);
            p.getInventory().setItem(8, is);
        }
    }
}
