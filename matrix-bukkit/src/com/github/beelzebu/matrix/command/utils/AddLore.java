package com.github.beelzebu.matrix.command.utils;

import com.github.beelzebu.matrix.api.commands.MatrixCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddLore extends MatrixCommand {

    public AddLore() {
        super("addlore", "matrix.staff.admin", false, "alore");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String locale = "";
        if (!(sender instanceof Player)) {
            sender.sendMessage(api.getString("No Console", locale));
            return;
        }
        locale = ((Player) sender).getLocale();
        if (!sender.hasPermission("matrix.staff.admin")) {
            sender.sendMessage(api.getString("No Permissions", locale));
            return;
        }
        Player p = (Player) sender;
        if (args.length > 0) {
            if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                p.sendMessage(api.getString("Item Utils.No Item", locale));
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
            p.sendMessage(api.getString("Item Utils.AddLore.Successful", locale).replaceAll("%line%", lore));
            return;
        }
        p.sendMessage(api.getString("Item Utils.AddLore.Help", locale));
    }
}
