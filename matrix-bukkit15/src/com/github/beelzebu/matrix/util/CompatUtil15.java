package com.github.beelzebu.matrix.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompatUtil15 extends CompatUtil {

    @Override
    public ItemStack getPlayerHead() {
        return new ItemStack(Material.PLAYER_HEAD);
    }

    @Override
    public ItemStack getPlayerHeadWall() {
        return new ItemStack(Material.PLAYER_WALL_HEAD);
    }

    @Override
    public ItemStack getPlayerHeadItem() {
        return new ItemStack(Material.PLAYER_HEAD);
    }

    @Override
    public ItemStack getGreenGlass() {
        return new ItemStack(Material.GREEN_STAINED_GLASS);
    }

    @Override
    public ItemStack getRedGlass() {
        return new ItemStack(Material.RED_STAINED_GLASS);
    }

    @Override
    public ItemStack getPurpleGlass() {
        return new ItemStack(Material.PURPLE_STAINED_GLASS);
    }

    @Override
    public ItemStack getPurpleGlassPane() {
        return new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    }

    @Override
    public Material getRedstoneComparator() {
        return Material.COMPARATOR;
    }

    @Override
    public ItemStack getGreenDye() {
        return new ItemStack(Material.GREEN_DYE);
    }

    @Override
    public Material getBookAndQuill() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public Material getStorageMinecart() {
        return Material.CHEST_MINECART;
    }

    @Override
    public void setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);
    }
}