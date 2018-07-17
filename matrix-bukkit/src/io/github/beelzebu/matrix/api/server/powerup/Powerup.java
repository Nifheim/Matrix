package io.github.beelzebu.matrix.api.server.powerup;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import io.github.beelzebu.matrix.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Powerup {

    private final EffectManager em = Main.getInstance().getEffectManager();
    private final Location l;
    private final String t;
    private final ItemStack i;
    private final Effect e;
    private final PotionEffect pe;
    private final int c;

    public Powerup(Location loc, String title, ItemStack item, Effect effect, PotionEffect potionEffect, int chance) {
        l = loc;
        t = title;
        i = item;
        e = effect;
        pe = potionEffect;
        c = chance;
    }

    /**
     * Generates a powerup from the given String
     *
     * @param string the String to generate the powerup in the format:
     *               world;x;y;z;title;material;data;potioneffecttype;duration;amplifier;chance
     * @param effect The effect to use.
     * @return a new Powerup.
     */
    public static Powerup fromString(String string, Effect effect) {
        String[] args = string.split(";");
        Location l = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
        String t = args[4];
        ItemStack i = new ItemStack(Material.matchMaterial(args[5]), 1, Short.valueOf(args[6]));
        PotionEffect pe = new PotionEffect(PotionEffectType.getByName(args[7]), Integer.valueOf(args[8]), Integer.valueOf(args[9]));
        int c = Integer.valueOf(args[10]);
        return new Powerup(l, t, i, effect, pe, c);
    }

    public Location getLocation() {
        return l;
    }

    public String getTitle() {
        return t;
    }

    public ItemStack getItemStack() {
        return i;
    }

    public Effect getEffect() {
        return e;
    }

    public PotionEffect getPotionEffect() {
        return pe;
    }

    public int getChance() {
        return c;
    }

    @Override
    public String toString() {
        return l.getWorld().getName() + ";" + l.getX() + ";" + l.getY() + ";" + l.getZ() + ";" + t + ";" + i.getType() + ";" + i.getDurability() + ";" + pe.getType() + ";" + pe.getDuration() + ";" + pe.getAmplifier() + ";" + c;
    }
}
