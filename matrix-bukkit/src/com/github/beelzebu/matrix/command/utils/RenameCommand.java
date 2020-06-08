package com.github.beelzebu.matrix.command.utils;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.util.StringUtils;
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
            sender.sendMessage(api.getString("No Console", ""));
            return;
        }
        String locale = ((Player) sender).getLocale();
        Player p = (Player) sender;
        if (args.length > 0) {
            if (p.getInventory().getItemInMainHand() == null) {
                p.sendMessage(api.getString("Item Utils.No Item", locale));
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
            p.sendMessage(api.getString("Item Utils.Rename.Successful", locale).replace("%name%", name.toString()));
            return;
        }
        p.sendMessage(api.getString("Item Utils.Rename.Help", locale));
    }
}
