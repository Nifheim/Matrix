package com.github.beelzebu.matrix.bukkit.command.utils;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class RemoveLoreCommand extends MatrixCommand {

    public RemoveLoreCommand() {
        super("removelore", "matrix.command.removelore", false, "rlore");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE));
            return;
        }
        Player p = (Player) sender;
        String locale = ((Player) sender).getLocale().substring(0, 2);
        if (args.length == 1) {
            p.getInventory().getItemInMainHand();
            try {
                int index = Integer.parseInt(args[0]);
                ItemStack item = p.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if ((meta.hasLore()) && (meta.getLore().size() > 0)) {
                    List<String> loreList = meta.getLore();
                    if (loreList.size() >= index) {
                        String lore = loreList.get(index);
                        loreList.remove(index);
                        meta.setLore(loreList);
                        item.setItemMeta(meta);
                        p.getInventory().setItemInMainHand(item);
                        p.sendMessage(I18n.tl(Message.ITEM_UTILS_REMOVE_LORE, locale).replace("%line%", lore));
                    } else {
                        p.sendMessage(I18n.tl(Message.ITEM_UTILS_REMOVE_NO_LINE, locale));
                    }
                } else {
                    p.sendMessage(I18n.tl(Message.ITEM_UTILS_REMOVE_NO_LORE, locale));
                }
            } catch (NumberFormatException e) {
                p.sendMessage(I18n.tl(Message.ITEM_UTILS_NO_NUMBER, locale).replace("%arg%", args[0]));
            }
        } else {
            p.sendMessage(I18n.tl(Message.ITEM_UTILS_REMOVE_LORE_USAGE, locale));
        }
    }
}
