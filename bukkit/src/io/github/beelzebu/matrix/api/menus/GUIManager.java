package io.github.beelzebu.matrix.api.menus;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Beelzebu
 */
public abstract class GUIManager {

    @Getter
    private static final Map<UUID, GUIManager> inventoriesByUUID = new HashMap<>();
    @Getter
    private static final Map<UUID, UUID> openInventories = Collections.synchronizedMap(new HashMap<>());
    private final MatrixAPI core = MatrixAPI.getInstance();
    @Getter
    private final Inventory inv;
    @Getter
    private final Map<Integer, GUIAction> actions;
    @Getter
    private final UUID uniqueId;
    @Getter
    @Setter
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
            inv = Bukkit.createInventory(null, type, core.rep(name));
        } else {
            inv = Bukkit.createInventory(null, size, core.rep(name));
        }
        actions = new HashMap<>();
        uniqueId = UUID.randomUUID();
        inventoriesByUUID.put(getUniqueId(), this);
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
        p.closeInventory();
        p.openInventory(inv);
        openInventories.put(p.getUniqueId(), getUniqueId());
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

    public Item getItem(IConfiguration config, String path) {
        Material material = Material.STONE;
        String materialPath = config.getString(path + ".Material").toUpperCase();
        if (Material.getMaterial(materialPath.split(":")[0]) != null) {
            material = Material.getMaterial(materialPath.split(":")[0]);
        }
        byte damage = Byte.valueOf(materialPath.split(":")[1]);
        int amount = Integer.valueOf(materialPath.split(":")[2]);
        String name = config.getString(path + ".Name");
        List<String> lore = config.getStringList(path + ".Lore");
        String soundPath = config.getString(path + ".Sound");
        String command = config.getString(path + ".Command");
        return new Item(new ItemBuilder(material, amount, core.rep(name)).damage(damage).lore(lore).build(), config.getInt(path + ".Slot"), player -> {
            if (command != null) {
                player.performCommand(command);
            }
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundPath.split(":")[0].replaceAll("\\.", "_").toUpperCase()), Integer.valueOf(soundPath.split(":")[1]), Integer.valueOf(soundPath.split(":")[2]));
            } catch (IllegalArgumentException ignore) { // invalid sound
            }
            if (config.getBoolean(path + ".Close")) {
                player.closeInventory();
            }
        });
    }

    public interface GUIAction {

        void click(Player p);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class Item {

        private final ItemStack itemStack;
        private final int slot;
        private final GUIAction guiAction;
    }
}
