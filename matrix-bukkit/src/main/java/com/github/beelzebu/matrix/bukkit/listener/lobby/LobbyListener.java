package com.github.beelzebu.matrix.bukkit.listener.lobby;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.ItemBuilder;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.bukkit.util.CompatUtil;
import com.github.beelzebu.matrix.bukkit.util.LocationUtils;
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
    private final MatrixAPI api;
    private final Map<MatrixPlayer, Set<PlayerOptionType>> playerOptions = new HashMap<>();

    public LobbyListener(MatrixBukkitBootstrap plugin) {
        api = plugin.getApi();
        this.plugin = plugin;
    }

    public static Set<Player> getEditMode() {
        return LobbyListener.editMode;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") != null && api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (editMode.contains(e.getPlayer())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || (api.getConfig().getString("Lobby World") != null && api.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
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
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
                    if (((Player) e.getDamager()).getInventory().getItem(EquipmentSlot.HEAD).getType() == Material.DIAMOND_HELMET && ((Player) e.getEntity()).getInventory().getItem(EquipmentSlot.HEAD).getType() == Material.DIAMOND_HELMET) {
                        return;
                    }
                } else {
                    Player damager = (Player) e.getDamager();
                    Player damaged = (Player) e.getEntity();
                    if (damager.getInventory().getHelmet() != null && damager.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET) {
                        if (damaged.getInventory().getHelmet() != null && damaged.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET) {
                            return;
                        }
                    }
                }
            }
            e.setCancelled(true);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRain(WeatherChangeEvent e) {
        if (e.toWeatherState()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        MatrixPlayer matrixPlayer = api.getPlayer(e.getPlayer().getUniqueId());
        matrixPlayer.setLoggedIn(true);
        Player player = e.getPlayer();
        setNormalItems(player);
        player.teleport(LocationUtils.locationFromString(LobbyData.getInstance().getConfig().getString("spawn", LocationUtils.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))));
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || player.getLocation().getWorld().getName().equalsIgnoreCase(api.getConfig().getString("Lobby World"))) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                // Set the level and xp to 0 for security reasons
                player.setLevel(0);
                player.setExp(0);
                player.setExhaustion(20);
                player.setSaturation(20);
                player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
                player.getInventory().setHeldItemSlot(4);
            }, 1);
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_9)) {
                    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
                }
                player.setGameMode(GameMode.ADVENTURE);
                for (PlayerOptionType optionType : matrixPlayer.getOptions()) {
                    if (optionType.equals(PlayerOptionType.SPEED)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, false, false));
                    } else if (optionType.equals(PlayerOptionType.FLY)) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }
                }
            }, 10);
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
        if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
            p.getInventory().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));
            p.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.AIR));
            p.getInventory().setItem(EquipmentSlot.LEGS, new ItemStack(Material.AIR));
            p.getInventory().setItem(EquipmentSlot.FEET, new ItemStack(Material.AIR));
        }
        String locale = api.getPlayer(p.getUniqueId()).getLastLocale();
        {
            ItemStack is = new ItemBuilder(Material.COMPASS, 1, I18n.tl(Message.LOBBY_ITEMS_SERVER_SELECTOR, locale)).build();
            p.getInventory().setItem(0, is);
        }
        {
            ItemStack is = new ItemBuilder(CompatUtil.getInstance().getRedstoneComparator(), 1, I18n.tl(Message.LOBBY_ITEMS_OPTIONS, locale)).build();
            p.getInventory().setItem(1, is);
        }
        {
            ItemStack is = new ItemBuilder(CompatUtil.getInstance().getPlayerHeadItem()).amount(1).displayname(I18n.tl(Message.LOBBY_ITEMS_PROFILE, locale)).build();
            SkullMeta meta = (SkullMeta) is.getItemMeta();
            if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
                meta.setOwningPlayer(p);
            } else {
                meta.setOwner(p.getName());
            }
            is.setItemMeta(meta);
            p.getInventory().setItem(8, is);
        }
    }
}