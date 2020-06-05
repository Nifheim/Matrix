package com.github.beelzebu.matrix.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Beelzebu
 */
public abstract class CompatUtil {

    private static CompatUtil INSTANCE;

    public static CompatUtil getInstance() {
        return INSTANCE;
    }

    public static void setInstance(CompatUtil instance) {
        INSTANCE = instance;
    }

    public abstract ItemStack getPlayerHead();

    public abstract ItemStack getPlayerHeadWall();

    public abstract ItemStack getPlayerHeadItem();

    public abstract ItemStack getGreenGlass();

    public abstract ItemStack getRedGlass();

    public abstract ItemStack getPurpleGlass();

    public abstract Material getRedstoneComparator();

    public abstract ItemStack getGreenDye();

    public abstract Material getBookAndQuill();

    public abstract Material getStorageMinecart();

    public abstract void setUnbreakable(ItemStack itemStack, boolean unbreakable);
}
