package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import cl.indiopikaro.jmatrix.api.Matrix;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

/**
 * @author Beelzebu
 */
public final class CompatUtil {

    private static CompatUtil INSTANCE;

    public static CompatUtil getInstance() {
        return INSTANCE == null ? (INSTANCE = new CompatUtil()) : INSTANCE;
    }

    public ItemStack getPlayerHeadBlock() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.PLAYER_WALL_HEAD);
        }
        return new ItemStack(Material.valueOf("SKULL"), 1, (byte) 3);
    }

    public ItemStack getPlayerHead() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
    }

    public ItemStack getPlayerHeadItem() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
    }

    public ItemStack getGreenGlass() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.GREEN_STAINED_GLASS);
        }
        return new ItemStack(Material.valueOf("STAINED_GLASS"), 1, (byte) 5);
    }

    public ItemStack getRedGlass() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.RED_STAINED_GLASS);
        }
        return new ItemStack(Material.valueOf("STAINED_GLASS"), 1, (byte) 14);
    }

    public ItemStack getPurpleGlass() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.PURPLE_STAINED_GLASS);
        }
        return new ItemStack(Material.valueOf("STAINED_GLASS"), 1, (byte) 10);
    }

    public ItemStack getPurpleGlassPane() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        }
        return new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (byte) 10);
    }

    public Material getRedstoneComparator() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return Material.COMPARATOR;
        }
        return Material.valueOf("REDSTONE_COMPARATOR");
    }

    public ItemStack getGreenDye() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return new ItemStack(Material.GREEN_DYE);
        }
        return new ItemStack(Material.valueOf("INK_SACK"), 1, (byte) 5);
    }

    public Material getBookAndQuill() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return Material.WRITABLE_BOOK;
        }
        return Material.valueOf("BOOK_AND_QUILL");
    }

    public Material getStorageMinecart() {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            return Material.CHEST_MINECART;
        }
        return Material.valueOf("STORAGE_MINECART");
    }

    public void setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);
    }

    public CompatUtil() {
        setup();
    }

    public enum MinecraftVersion {
        MINECRAFT_1_8(1),
        MINECRAFT_1_9(2),
        MINECRAFT_1_10(3),
        MINECRAFT_1_11(4),
        MINECRAFT_1_12(5),
        MINECRAFT_1_13(6),
        MINECRAFT_1_14(7),
        MINECRAFT_1_15(8),
        MINECRAFT_1_16(9);

        private final int id;

        MinecraftVersion(int id) {
            this.id = id;
        }

        public boolean isAfterOrEq(MinecraftVersion another) {
            return id >= another.id;
        }
    }

    public static MinecraftVersion VERSION;
    private Method localeMethod;

    public void setup() {
        Matrix.getLogger().info("Detected " + getRawVersion() + " server version.");
        switch (getMinorVersion()) {
            case 8:
                VERSION = MinecraftVersion.MINECRAFT_1_8;
                break;
            case 9:
                VERSION = MinecraftVersion.MINECRAFT_1_9;
                break;
            case 10:
                VERSION = MinecraftVersion.MINECRAFT_1_10;
                break;
            case 11:
                VERSION = MinecraftVersion.MINECRAFT_1_11;
                break;
            case 12:
                VERSION = MinecraftVersion.MINECRAFT_1_12;
                break;
            case 13:
                VERSION = MinecraftVersion.MINECRAFT_1_13;
                break;
            case 14:
                VERSION = MinecraftVersion.MINECRAFT_1_14;
                break;
            case 15:
                VERSION = MinecraftVersion.MINECRAFT_1_15;
                break;
            case 16:
                VERSION = MinecraftVersion.MINECRAFT_1_16;
                break;
            default:
                Matrix.getLogger().info("We don't have official support for: " + getRawVersion() + " using support for 1.15");
                VERSION = MinecraftVersion.MINECRAFT_1_15;
                break;
        }
    }


    public String getLocale(Player player) {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_12)) {
            return player.getLocale();
        } else { // this doesn't exists in 1.8
            if (localeMethod != null) {
                try {
                    return (String) localeMethod.invoke(player.spigot());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
            try {
                return (String) (localeMethod = Player.Spigot.class.getMethod("getLocale")).invoke(player.spigot());
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return "en";
    }

    public void setPotionType(PotionMeta meta, PotionType type) {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            meta.setBasePotionData(new PotionData(type));
        } else {
            if (type.getEffectType() != null) {
                meta.setMainEffect(type.getEffectType()); // it was deprecated in 1.13
            }
        }
    }

    public Enchantment getEnchantment(String string) {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            Optional<Enchantment> enchantmentOptional = Stream.of(Enchantment.values()).filter(enchantment -> enchantment.getKey().getKey().equalsIgnoreCase(string)).findFirst();
            if (enchantmentOptional.isPresent()) {
                return enchantmentOptional.get();
            }
        } else {
            if (Enchantment.getByName(string.toUpperCase()) != null) {
                return Enchantment.getByName(string.toUpperCase());
            }
        }
        return null;
    }


    public ItemStack setDamage(ItemStack itemStack, int damage) {
        if (VERSION.isAfterOrEq(MinecraftVersion.MINECRAFT_1_13)) {
            if (itemStack.getItemMeta() instanceof Damageable) {
                Damageable meta = (Damageable) itemStack.getItemMeta();
                meta.setDamage(damage);
                itemStack.setItemMeta((ItemMeta) meta);
            }
        } else {
            itemStack.setDurability((short) damage);
        }
        return itemStack;
    }


    private String getRawVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
    }

    private int getMinorVersion() {
        String ver = getRawVersion();
        int verInt = -1;
        try {
            verInt = Integer.parseInt(ver.split("_")[1]);
        } catch (IllegalArgumentException e) {
            Bukkit.getScheduler().runTask(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class), () -> {
                Matrix.getLogger().info("An error occurred getting server version, please contact developer.");
                Matrix.getLogger().info("Detected version " + ver);
            });
        }
        return verInt;
    }
}
