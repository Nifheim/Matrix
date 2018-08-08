package io.github.beelzebu.matrix.manager;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.WarpEffect;
import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.api.server.lobby.LobbyData;
import io.github.beelzebu.matrix.api.server.powerup.Powerup;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * @author Beelzebu
 */
public class PowerupManager {

    private static PowerupManager instance;
    private final Main plugin;
    @Getter
    private final Set<Powerup> powerups = new HashSet<>();

    private PowerupManager(Main main) {
        plugin = main;
        LobbyData.getInstance().getConfig().getStringList("Powerups").forEach(powerup -> powerups.add(Powerup.fromString(powerup, new WarpEffect(new EffectManager(plugin)))));
    }

    public static PowerupManager getInstance() {
        return instance == null ? instance = new PowerupManager(Main.getPlugin(Main.class)) : instance;
    }

    public void spawnPowerup(Location loc, String title, ItemStack item, Effect effect, PotionEffect potionEffect, int chance) {
        if (new Random().nextInt(100) > chance) {
            return;
        }
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
    }

    public synchronized void spawnPowerup(Powerup powerup) {
        if (new Random().nextInt(100) > powerup.getChance()) {
            return;
        }
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> powerups.remove(powerup), 60);
    }
}
