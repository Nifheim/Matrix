package io.github.beelzebu.matrix.commands.utils;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Rename extends MatrixCommand {

    public Rename() {
        super("rename", "matrix.vip.lord", "ren", "renombrar");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String locale = "";
        if (!(sender instanceof Player)) {
            sender.sendMessage(core.getString("No Console", locale));
            return;
        }
        locale = ((Player) sender).getLocale();
        Player p = (Player) sender;
        if (args.length > 0) {
            if (p.getInventory().getItemInMainHand() == null) {
                p.sendMessage(core.getString("Item Utils.No Item", locale));
                return;
            }
            String name = "";
            for (String arg : args) {
                name = name + arg + " ";
            }
            name = name.replaceAll("&", "§") + "strtdlµ";
            name = name.replace(" strtdlµ", "");
            ItemStack item = p.getItemInHand();
            ItemMeta meta = p.getItemInHand().getItemMeta();
            meta.setDisplayName(name);
            item.setItemMeta(meta);
            p.setItemInHand(item);
            p.sendMessage(core.getString("Item Utils.Rename.Successful", locale).replaceAll("%name%", name));
            return;
        }
        p.sendMessage(core.getString("Item Utils.Rename.Help", locale));
    }
}
