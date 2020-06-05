package com.github.beelzebu.matrix.api.menus;

import com.github.beelzebu.matrix.api.ItemBuilder;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public abstract class GUIManager {

    private static final Map<UUID, GUIManager> inventoriesByUUID = new HashMap<>();
    private static final Map<UUID, UUID> openInventories = Collections.synchronizedMap(new HashMap<>());
    private final MatrixAPI api = Matrix.getAPI();
    private final Inventory inv;
    private final Map<Integer, GUIAction> actions;
    private final UUID uniqueId;
    private ItemStack opener;

    public GUIManager(int size, String name) {
        this(size, name, null);
    }

    public GUIManager(int size, String name, InventoryType type) {
        if (size < 9) {
            size *= 9;
        }
        if ((size % 9) > 0) {
            if (size > 53) {
                size -= size - 53;
            } else {
                size += 9 - (size % 9);
            }
        }
        if (type != null && !type.equals(InventoryType.CHEST)) {
            inv = Bukkit.createInventory(null, type, StringUtils.replace(name));
        } else {
            inv = Bukkit.createInventory(null, size, StringUtils.replace(name));
        }
        actions = new HashMap<>();
        uniqueId = UUID.randomUUID();
        inventoriesByUUID.put(getUniqueId(), this);
    }

    public static Map<UUID, GUIManager> getInventoriesByUUID() {
        return GUIManager.inventoriesByUUID;
    }

    public static Map<UUID, UUID> getOpenInventories() {
        return GUIManager.openInventories;
    }

    public final void setItem(Item item) {
        setItem(item.getSlot(), item.getItemStack(), item.getGuiAction());
    }

    public final void setItem(int slot, ItemStack is, GUIAction action) {
        inv.setItem(slot, is);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    public final void setItem(int slot, ItemStack is) {
        setItem(slot, is, null);
    }

    public final synchronized void open(Player p) {
        Bukkit.getScheduler().runTask((Plugin) api.getPlugin().getBootstrap(), () -> {
            p.closeInventory();
            p.openInventory(inv);
            openInventories.put(p.getUniqueId(), getUniqueId());
        });
    }

    public void delete() {
        Bukkit.getOnlinePlayers().forEach((p) -> {
            UUID u = openInventories.get(p.getUniqueId());
            if (u.equals(getUniqueId())) {
                p.closeInventory();
            }
        });
        inventoriesByUUID.remove(getUniqueId());
    }

    public Item getItem(AbstractConfig config, String path) {
        Material material = Material.STONE;
        String materialPath = config.getString(path + ".Material").toUpperCase();
        if (Material.getMaterial(materialPath.split(":")[0]) != null) {
            material = Material.getMaterial(materialPath.split(":")[0]);
        }
        byte damage = Byte.parseByte(materialPath.split(":")[1]);
        int amount = Integer.parseInt(materialPath.split(":")[2]);
        String name = config.getString(path + ".Name");
        List<String> lore = config.getStringList(path + ".Lore");
        String soundPath = config.getString(path + ".Sound");
        String command = config.getString(path + ".Command");
        return new Item(new ItemBuilder(material, amount, StringUtils.replace(name)).damage(damage).lore(lore).build(), config.getInt(path + ".Slot"), player -> {
            if (command != null) {
                player.performCommand(command);
            }
            if (soundPath != null) {
                try {
                    player.playSound(player.getLocation(),
                            Sound.valueOf(soundPath.split(":")[0].replaceAll("\\.", "_").toUpperCase()),
                            Integer.parseInt(soundPath.split(":")[1]),
                            Integer.parseInt(soundPath.split(":")[2])
                    );
                } catch (IllegalArgumentException ignore) { // invalid sound
                }
            }
            if (config.getBoolean(path + ".Close")) {
                player.closeInventory();
            }
        });
    }

    public Inventory getInv() {
        return inv;
    }

    public Map<Integer, GUIAction> getActions() {
        return actions;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public ItemStack getOpener() {
        return opener;
    }

    public void setOpener(ItemStack opener) {
        this.opener = opener;
    }

    public interface GUIAction {

        void click(Player p);
    }

    public class Item {

        private final ItemStack itemStack;
        private final int slot;
        private final GUIAction guiAction;

        public Item(ItemStack itemStack, int slot, GUIManager.GUIAction guiAction) {
            this.itemStack = itemStack;
            this.slot = slot;
            this.guiAction = guiAction;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getSlot() {
            return slot;
        }

        public GUIManager.GUIAction getGuiAction() {
            return guiAction;
        }
    }
}
