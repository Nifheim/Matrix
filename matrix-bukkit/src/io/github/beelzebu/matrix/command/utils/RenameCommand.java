package io.github.beelzebu.matrix.command.utils;

import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCommand extends MatrixCommand {

    public RenameCommand() {
        super("rename", "matrix.command.rename", "ren", "renombrar");
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