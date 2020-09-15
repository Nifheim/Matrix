package com.github.beelzebu.matrix.bukkit.manager;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.api.server.powerup.Powerup;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * @author Beelzebu
 */
public class PowerupManager {

    private static PowerupManager instance;
    private final MatrixBukkitBootstrap plugin;
    private final Set<Powerup> powerups = new HashSet<>();

    private PowerupManager(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
        LobbyData.getInstance().getConfig().getStringList("Powerups").forEach(powerup -> powerups.add(Powerup.fromString(powerup)));
    }

    public static PowerupManager getInstance() {
        return instance == null ? instance = new PowerupManager(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class)) : instance;
    }

    public void spawnPowerup(Location loc, String title, ItemStack item, PotionEffect potionEffect, int chance) {
        if (new Random().nextInt(100) > chance) {
            return;
        }
        /*
        Hologram holo = HologramsAPI.createHologram(plugin, loc);
        holo.getVisibilityManager().setVisibleByDefault(true);
        holo.appendTextLine(title);
        ItemLine iLine = holo.appendItemLine(item);
        iLine.setPickupHandler(player -> {
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 2);
            player.addPotionEffect(potionEffect);
            effect.iterations = 60;
            effect.setLocation(loc);
            effect.start();
            holo.delete();
        });
         */
    }

    public synchronized void spawnPowerup(Powerup powerup) {
        if (new Random().nextInt(100) > powerup.getChance()) {
            return;
        }/*
        Hologram holo = HologramsAPI.createHologram(plugin, powerup.getLocation());
        holo.getVisibilityManager().setVisibleByDefault(true);
        holo.appendTextLine(powerup.getTitle());
        ItemLine iLine = holo.appendItemLine(powerup.getItemStack());
        iLine.setTouchHandler(player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 2);
            player.addPotionEffect(powerup.getPotionEffect());
            Effect e = powerup.getEffect();
            e.setLocation(player.getLocation());
            e.start();
            holo.delete();
            powerups.add(powerup);
        });
        */
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> powerups.remove(powerup), 60);
    }

    public Set<Powerup> getPowerups() {
        return powerups;
    }
}
