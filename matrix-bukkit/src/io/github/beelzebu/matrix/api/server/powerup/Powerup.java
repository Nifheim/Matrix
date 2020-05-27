package io.github.beelzebu.matrix.api.server.powerup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Powerup {

    private final Location l;
    private final String t;
    private final ItemStack i;
    private final PotionEffect pe;
    private final int c;

    public Powerup(Location loc, String title, ItemStack item, PotionEffect potionEffect, int chance) {
        l = loc;
        t = title;
        i = item;
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
    public static Powerup fromString(String string) {
        String[] args = string.split(";");
        Location l = new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        String t = args[4];
        ItemStack i = new ItemStack(Material.matchMaterial(args[5]), 1, Short.parseShort(args[6]));
        PotionEffect pe = new PotionEffect(PotionEffectType.getByName(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]));
        int c = Integer.parseInt(args[10]);
        return new Powerup(l, t, i, pe, c);
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
