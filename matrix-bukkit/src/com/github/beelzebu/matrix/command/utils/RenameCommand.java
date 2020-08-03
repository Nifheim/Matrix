package com.github.beelzebu.matrix.command.utils;

import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Beelzebu
 */
public class RenameCommand extends MatrixCommand {

    public RenameCommand() {
        super("rename", "matrix.command.rename", false, "ren", "renombrar");
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
            if (p.getInventory().getItemInMainHand() == null) {
                p.sendMessage(I18n.tl(Message.ITEM_UTILS_NO_ITEM, locale));
                return;
            }
            StringBuilder name = new StringBuilder();
            for (String arg : args) {
                name.append(arg).append(" ");
            }
            ItemStack item = p.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(StringUtils.replace(name.substring(0, name.length() - 1)));
            item.setItemMeta(meta);
            p.getInventory().setItemInMainHand(item);
            p.sendMessage(I18n.tl(Message.ITEM_UTILS_RENAME, locale).replace("%name%", name.toString()));
            return;
        }
        p.sendMessage(I18n.tl(Message.ITEM_UTILS_RENAME_USAGE, locale));
    }
}
