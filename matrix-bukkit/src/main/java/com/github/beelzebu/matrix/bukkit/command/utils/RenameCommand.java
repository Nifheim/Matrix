package com.github.beelzebu.matrix.bukkit.command.utils;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class RenameCommand extends MatrixCommand {

    public RenameCommand() {
        super("rename", "matrix.command.rename", false, "ren", "renombrar");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE));
            return;
        }
        String locale = ((Player) sender).getLocale().substring(0, 2);
        Player p = (Player) sender;
        if (args.length > 0) {
            if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                p.sendMessage(I18n.tl(Message.ITEM_UTILS_NO_ITEM, locale));
                return;
            }
            String name = String.join(" ", args);
            ItemStack item = p.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
            p.getInventory().setItemInMainHand(item);
            p.sendMessage(I18n.tl(Message.ITEM_UTILS_RENAME, locale).replace("%name%", ChatColor.translateAlternateColorCodes('&', name.toString())));
            return;
        }
        p.sendMessage(I18n.tl(Message.ITEM_UTILS_RENAME_USAGE, locale));
    }
}
