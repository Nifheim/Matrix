package io.github.beelzebu.matrix.api.menus;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.util.StringUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfirmGUI extends GUIManager {

    private final MatrixAPI api = Matrix.getAPI();
    private final Player player;
    private final ItemStack item;
    private final String name;
    private final List<String> lore;
    private final GUIAction accept;
    private final GUIAction decline;

    /**
     * Generate the gui with the following parameters:
     *
     * @param player  The player to generate the GUI
     * @param title   The title of the GUI
     * @param item    The ItemStack to use in the middle
     * @param name    The name of the ItemStack
     * @param lore    The lore of the ItemStack
     * @param accept  The action to execute on confirm
     * @param decline The action to execute on decline
     */
    public ConfirmGUI(Player player, String title, ItemStack item, String name, List<String> lore, GUIAction accept, GUIAction decline) {
        super(9, title);
        this.player = player;
        this.item = item;
        this.name = name;
        this.lore = lore;
        this.accept = accept;
        this.decline = decline;
        setItems();
    }

    private void setItems() {
        if (player == null) {
            return;
        }
        {
            ItemStack is = new ItemStack(Material.STAINED_GLASS, 1, (short) 5);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(api.getString("Utils.GUI.Confirm.Accept", player.getLocale()));
            is.setItemMeta(meta);
            setItem(2, is, accept);
        }
        {
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            meta.setDisplayName(StringUtils.replace(name));
            meta.setLore(lore);
            item.setItemMeta(meta);
            setItem(4, item);
        }
        {
            ItemStack is = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(api.getString("Utils.GUI.Confirm.Decline", player.getLocale()));
            is.setItemMeta(meta);
            setItem(6, is, decline);
        }
    }
}
