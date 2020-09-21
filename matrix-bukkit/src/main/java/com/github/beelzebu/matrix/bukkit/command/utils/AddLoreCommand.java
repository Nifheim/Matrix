package com.github.beelzebu.matrix.bukkit.command.utils;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Beelzebu
 */
public class AddLoreCommand extends MatrixCommand {

    public AddLoreCommand() {
        super("addlore", "matrix.command.addlore", false, "alore");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE));
            return;
        }
        String locale = api.getPlayer(((Player) sender).getUniqueId()).getLastLocale();
        Player p = (Player) sender;
        if (args.length > 0) {
            if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                p.sendMessage(I18n.tl(Message.ITEM_UTILS_NO_ITEM, locale));
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String arg : args) {
                stringBuilder.append(arg).append(" ");
            }
            String lore = stringBuilder.toString();
            lore = lore.substring(0, lore.length() - 1).replaceAll("&", "§");
            ItemStack item = p.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                List<String> loreList = meta.getLore();
                loreList.add(lore);
                meta.setLore(loreList);
            } else {
                List<String> loreList = new ArrayList<>();
                loreList.add(lore);
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
            p.getInventory().setItemInMainHand(item);
            p.sendMessage(I18n.tl(Message.ITEM_UTILS_ADD_LORE, locale).replace("%line%", lore));
            return;
        }
        p.sendMessage(I18n.tl(Message.ITEM_UTILS_ADD_LORE_USAGE, locale));
    }
}
