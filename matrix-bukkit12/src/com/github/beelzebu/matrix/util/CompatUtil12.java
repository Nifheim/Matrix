package com.github.beelzebu.matrix.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompatUtil12 extends CompatUtil {

    @Override
    public ItemStack getPlayerHead() {
        return new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
    }

    @Override
    public ItemStack getPlayerHeadWall() {
        return new ItemStack(Material.SKULL, 1, (byte) 3);
    }

    @Override
    public ItemStack getPlayerHeadItem() {
        return new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
    }

    @Override
    public ItemStack getGreenGlass() {
        return new ItemStack(Material.STAINED_GLASS, 1, (byte) 5);
    }

    @Override
    public ItemStack getRedGlass() {
        return new ItemStack(Material.STAINED_GLASS, 1, (byte) 14);
    }

    @Override
    public ItemStack getPurpleGlass() {
        return new ItemStack(Material.STAINED_GLASS, 1, (byte) 10);
    }

    @Override
    public Material getRedstoneComparator() {
        return Material.REDSTONE_COMPARATOR_OFF;
    }

    @Override
    public ItemStack getGreenDye() {
        return new ItemStack(Material.INK_SACK, 1, (byte) 5);
    }

    @Override
    public Material getBookAndQuill() {
        return Material.BOOK_AND_QUILL;
    }

    @Override
    public Material getStorageMinecart() {
        return Material.STORAGE_MINECART;
    }

    @Override
    public void setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);
    }
}