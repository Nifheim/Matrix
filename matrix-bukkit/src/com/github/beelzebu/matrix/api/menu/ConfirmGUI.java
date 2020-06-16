package com.github.beelzebu.matrix.api.menu;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.util.CompatUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Beelzebu
 */
public class ConfirmGUI extends GUIManager {

    private final ItemStack item;
    private final String name;
    private final List<String> lore;
    private final GUIAction accept;
    private final GUIAction decline;
    private final String locale;

    /**
     * Generate the gui with the following parameters:
     *
     * @param player  The player to generate the GUI
     * @param item    The ItemStack to use in the middle
     * @param name    The name of the ItemStack
     * @param lore    The lore of the ItemStack
     * @param accept  The action to execute on confirm
     * @param decline The action to execute on decline
     */
    public ConfirmGUI(MatrixPlayer player, ItemStack item, String name, List<String> lore, GUIAction accept, GUIAction decline) {
        super(9, I18n.tl(Message.MENU_UTIL_CONFIRM_TITLE, player.getLastLocale()));
        this.item = item;
        this.name = name;
        this.lore = lore;
        this.accept = accept;
        this.decline = decline;
        locale = player.getLastLocale();
        setItems();
    }

    private void setItems() {
        {
            ItemStack is = CompatUtil.getInstance().getGreenGlass();
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(I18n.tl(Message.MENU_UTIL_CONFIRM_ACCEPT_NAME, locale));
            meta.setLore(Arrays.asList(I18n.tls(Message.MENU_UTIL_CONFIRM_ACCEPT_LORE, locale)));
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
            ItemStack is = CompatUtil.getInstance().getRedGlass();
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(I18n.tl(Message.MENU_UTIL_CONFIRM_DECLINE_NAME, locale));
            meta.setLore(Arrays.asList(I18n.tls(Message.MENU_UTIL_CONFIRM_DECLINE_LORE, locale)));
            is.setItemMeta(meta);
            setItem(6, is, decline);
        }
    }
}
