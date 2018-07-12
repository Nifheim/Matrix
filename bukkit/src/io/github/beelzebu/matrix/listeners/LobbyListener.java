package io.github.beelzebu.matrix.listeners;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.HelixEffect;
import de.slikey.effectlib.effect.LoveEffect;
import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.manager.PowerupManager;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import io.github.beelzebu.matrix.server.lobby.LaunchPad;
import io.github.beelzebu.matrix.server.lobby.LobbyData;
import io.github.beelzebu.matrix.utils.ServerType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * @author Beelzebu
 */
public class LobbyListener implements Listener {

    @Getter
    private static final Set<Player> editMode = new HashSet<>();
    private final Main plugin;
    private final MatrixAPI core = MatrixAPI.getInstance();
    private final LobbyData data = LobbyData.getInstance();
    private final PowerupManager powerups;
    private final List<LaunchPad> launchpads;
    private final EffectManager em;
    private final Set<Player> pvpPlayers = new HashSet<>();
    private final Set<Player> normalPlayers = new HashSet<>();
    private final Map<MatrixPlayer, Set<PlayerOptionType>> playerOptions = new HashMap<>();

    public LobbyListener(Main main) {
        plugin = main;
        powerups = PowerupManager.getInstance();
        launchpads = data.getLaunchpads();
        em = plugin.getEffectManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (editMode.contains(e.getPlayer())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (editMode.contains(e.getPlayer())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPvP(EntityDamageByEntityEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getEntity().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getEntity().getWorld().getName()))) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                MatrixPlayer damager = core.getPlayer(e.getDamager().getUniqueId());
                MatrixPlayer victim = core.getPlayer(e.getEntity().getUniqueId());
                if (!canPvP(damager) || !canPvP(victim)) {
                    e.setCancelled(true);
                } else {
                    playerOptions.put(damager, damager.getActiveOptions());
                    damager.setOption(PlayerOptionType.FLY, false);
                    damager.setOption(PlayerOptionType.SPEED, false);
                    playerOptions.put(victim, victim.getActiveOptions());
                    victim.setOption(PlayerOptionType.FLY, false);
                    victim.setOption(PlayerOptionType.SPEED, false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getEntity().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getEntity().getWorld().getName()))) {
            if (e.getCause().equals(DamageCause.FALL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getEntity().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getEntity().getWorld().getName()))) {
            e.setDeathMessage("");
            if (e.getEntity().getKiller() == null) {
                return;
            }
            e.getDrops().clear();
            LoveEffect effect = new LoveEffect(em);
            powerups.spawnPowerup(
                    e.getEntity().getLocation().add(0, 0.9, 0),
                    "§6§lREGENERACIÓN",
                    new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1),
                    effect,
                    new PotionEffect(PotionEffectType.REGENERATION, 100, 5),
                    70
            );
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
        MatrixPlayer np = core.getPlayer(e.getPlayer().getUniqueId());
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (data.getSpawn() != null) {
                p.teleport(data.getSpawn());
            }
        }, 2);
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                core.getRedis().saveStats(core.getPlayer(p.getUniqueId()), core.getServerInfo().getServerName(), core.getServerInfo().getServerType(), null);
            });
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                int level = NetworkXP.getLevelForPlayer(p.getUniqueId());
                int xp = (int) (NetworkXP.MCEXP.getXPForPlayer(p.getName()) - NetworkXP.MCEXP.getXPForLevel(level));
                // Set the level and xp to 0 for security reasons
                p.setLevel(0);
                p.setExp(0);
                p.setLevel(level);
                p.giveExp(xp);
                p.setExhaustion(20);
                p.setSaturation(20);
                p.getActivePotionEffects().forEach((pe) -> {
                    p.removePotionEffect(pe.getType());
                });
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
                setPvP(p, false);
            }, 10);
        } else {
            playerOptions.put(np, np.getActiveOptions());
            np.getActiveOptions().forEach(option -> {
                np.setOption(option, false);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        MatrixPlayer np = core.getPlayer(p.getUniqueId());
        setPvP(p, false);
        if (pvpPlayers.contains(p)) {
            pvpPlayers.remove(p);
        }
        if (playerOptions.containsKey(np)) {
            playerOptions.get(np).forEach(option -> {
                np.setOption(option, true);
            });
            playerOptions.remove(np);
        }
        if (editMode.contains(p)) {
            editMode.remove(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            Player p = e.getPlayer();
            Location loc = e.getTo();
            if (e.getTo().getY() <= 0) {
                p.teleport(data.getSpawn());
            }
            if (e.getFrom().distance(loc) > 0.5) {
                if (canPvP(core.getPlayer(p.getUniqueId()))) {
                    setPvP(p, true);
                } else {
                    setPvP(p, false);
                }
            }

            // Delete holograms manually, seems that only the last holo is removed.
            HologramsAPI.getHolograms(plugin).forEach(holo -> {
                if (holo.getLocation().distance(loc) < 1 && !holo.isDeleted()) {
                    holo.delete();
                }
            });

            // If there are no launchpads, the following code is useless
            if (launchpads.isEmpty()) {
                return;
            }
            for (LaunchPad lp : launchpads) {
                if (lp.getLocation().distance(loc) < 0.5) {
                    Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        List<Sound> sounds = Arrays.asList(
                                Sound.ENTITY_FIREWORK_LAUNCH,
                                Sound.ENTITY_FIREWORK_LAUNCH,
                                Sound.ENTITY_FIREWORK_LAUNCH
                        );
                        sounds.forEach((sound) -> {
                            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                                int pitch = 0;
                                p.playSound(loc, sound, 10, pitch);
                                if (sound.equals(Sound.ENTITY_FIREWORK_LAUNCH)) {
                                    pitch++;
                                }
                            }, 6);
                        });
                        if (lp.isEffect()) {
                            HelixEffect effect = new HelixEffect(em);
                            effect.setLocation(loc.getBlock().getLocation().add(0.5, 0.5, 0.5));
                            effect.radius = 1;
                            effect.iterations = 5;
                            effect.setTargetPlayer(p);
                            effect.start();
                        }
                        p.setVelocity(new Vector(0, 0, 0));
                        p.setVelocity(lp.getVector());
                        p.playSound(loc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 10, 1);
                    });
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        MatrixPlayer p = core.getPlayer(e.getPlayer().getUniqueId());
        if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getConfig().getString("Lobby World") == null ? e.getPlayer().getWorld().getName() == null : core.getConfig().getString("Lobby World").equals(e.getPlayer().getWorld().getName()))) {
            if (playerOptions.containsKey(p)) {
                playerOptions.get(p).forEach(option -> {
                    p.setOption(option, true);
                });
            }
            setPvP(e.getPlayer(), false);
        } else {
            playerOptions.put(p, p.getActiveOptions());
            p.getActiveOptions().forEach(option -> {
                p.setOption(option, false);
            });
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setFoodLevel(20);
        }
    }

    private boolean canPvP(MatrixPlayer p) {
        try {
            WorldGuardPlugin worldguard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
            ApplicableRegionSet set = (worldguard.getRegionManager(Bukkit.getPlayer(p.getUniqueId()).getWorld()).getApplicableRegions(Bukkit.getPlayer(p.getUniqueId()).getLocation()));
            LocalPlayer localPlayer = worldguard.wrapPlayer(Bukkit.getPlayer(p.getUniqueId()));
            return set.queryState(localPlayer, DefaultFlag.PVP) != StateFlag.State.DENY && set.queryState(localPlayer, DefaultFlag.INVINCIBILITY) != StateFlag.State.ALLOW;
        } catch (Exception ex) {
            return true;
        }
    }

    private void setPvP(Player p, Boolean pvp) {
        if (pvp && !pvpPlayers.contains(p)) {
            setPvPItems(p);
            pvpPlayers.add(p);
            normalPlayers.remove(p);
        } else if (!pvp) {
            if (pvpPlayers.contains(p)) {
                pvpPlayers.remove(p);
            }
            if (!normalPlayers.contains(p)) {
                normalPlayers.add(p);
                setNormalItems(p);
            }
        }
    }

    private void setPvPItems(Player p) {
        p.getInventory().clear();
        p.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
        p.getInventory().setItem(EquipmentSlot.HEAD, new ItemStack(Material.IRON_HELMET));
        p.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setItem(EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setItem(EquipmentSlot.FEET, new ItemStack(Material.IRON_BOOTS));
    }

    private void setNormalItems(Player p) {
        p.getInventory().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.LEGS, new ItemStack(Material.AIR));
        p.getInventory().setItem(EquipmentSlot.FEET, new ItemStack(Material.AIR));
        {
            ItemStack is = new ItemBuilder(Material.COMPASS, 1, core.getString("Lobby items.Server selector.Name", p.getLocale())).build();
            p.getInventory().setItem(0, is);
        }
        {
            ItemStack is = new ItemBuilder(Material.REDSTONE_COMPARATOR, 1, core.getString("Lobby items.Options.Name", p.getLocale())).build();
            p.getInventory().setItem(1, is);
        }
        {
            ItemStack is = new ItemBuilder(Material.SKULL_ITEM, 1, core.getString("Lobby items.Profile.Name", p.getLocale())).durability((short) 3).build();
            SkullMeta meta = (SkullMeta) is.getItemMeta();
            meta.setOwningPlayer(p);
            is.setItemMeta(meta);
            p.getInventory().setItem(8, is);
        }
    }

    @EventHandler
    public void onLocaleChange(PlayerLocaleChangeEvent e) {
        setPvP(e.getPlayer(), pvpPlayers.contains(e.getPlayer()));
    }
}
