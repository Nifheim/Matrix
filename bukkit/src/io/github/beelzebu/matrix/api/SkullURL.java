package io.github.beelzebu.matrix.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkullURL {

    private static final Random random = new Random();
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Method getWorldHandle;
    private static Method getWorldTileEntity;
    private static Method setGameProfile;

    public SkullURL() {
        if (getWorldHandle == null || getWorldTileEntity == null || setGameProfile == null) {
            try {
                getWorldHandle = getCraftClass("CraftWorld").getMethod("getHandle");
                getWorldTileEntity = getMCClass("WorldServer").getMethod("getTileEntity", int.class, int.class, int.class);
                setGameProfile = getMCClass("TileEntitySkull").getMethod("setGameProfile", GameProfile.class);
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static ItemStack getCustomSkull(String url) {
        ItemStack localItemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url.isEmpty()) {
            return localItemStack;
        } else {
            ItemMeta localItemMeta = localItemStack.getItemMeta();
            GameProfile localGameProfile = new GameProfile(UUID.randomUUID(), (String) null);
            byte[] arrayOfByte = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
            localGameProfile.getProperties().put("textures", new Property("textures", new String(arrayOfByte)));
            Field localField = null;

            try {
                localField = localItemMeta.getClass().getDeclaredField("profile");
            } catch (NoSuchFieldException | SecurityException ex) {
                ex.printStackTrace();
            }

            localField.setAccessible(true);

            try {
                localField.set(localItemMeta, localGameProfile);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }

            localItemStack.setItemMeta(localItemMeta);
            return localItemStack;
        }
    }

    public static void setSkullWithNonPlayerProfile(String skinURL, boolean randomName, Block skull) {
        if (skull.getType() != Material.SKULL) {
            throw new IllegalArgumentException("Block must be a skull.");
        }
        Skull s = (Skull) skull.getState();
        try {
            setSkullProfile(s, getNonPlayerProfile(skinURL, randomName));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            e.printStackTrace();
        }
        skull.getWorld().refreshChunk(skull.getChunk().getX(), skull.getChunk().getZ());
    }

    private static void setSkullProfile(Skull skull, GameProfile someGameprofile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object world = getWorldHandle.invoke(skull.getWorld());
        Object tileSkull = getWorldTileEntity.invoke(world, skull.getX(), skull.getY(), skull.getZ());
        setGameProfile.invoke(tileSkull, someGameprofile);
    }

    public static GameProfile getNonPlayerProfile(String skinURL, boolean randomName) {
        GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), randomName ? getRandomString(16) : null);
        newSkinProfile.getProperties().put("textures", new Property("textures", Base64.encodeBase64String(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes())));
        return newSkinProfile;
    }

    public static String getRandomString(int length) {
        StringBuilder b = new StringBuilder(length);
        for (int j = 0; j < length; j++) {
            b.append(chars.charAt(random.nextInt(chars.length())));
        }
        return b.toString();
    }

    private static Class<?> getMCClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String className = "net.minecraft.server." + version + name;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private static Class<?> getCraftClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String className = "org.bukkit.craftbukkit." + version + name;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
