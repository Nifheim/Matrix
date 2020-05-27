package io.github.beelzebu.matrix.command.utils;

import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RemoveLore extends MatrixCommand {

    public RemoveLore() {
        super("removelore", "matrix.staff.admin", "rlore");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String locale = "";
        if (sender instanceof Player) {
            Player p = (Player) sender;
            locale = p.getLocale();
            if (args.length == 1) {
                if (p.getInventory().getItemInMainHand() != null) {
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
                                p.sendMessage(api.getString("Item Utils.RemoveLore.Successful", locale).replaceAll("%line%", lore));
                            } else {
                                p.sendMessage(StringUtils.replace("%prefix% §4Error:§c Este item no tiene ningún lore en esta linea"));
                            }
                        } else {
                            p.sendMessage(StringUtils.replace("%prefix% §4Error:§c Este item no tiene lores"));
                        }
                    } catch (NumberFormatException e) {
                        p.sendMessage(api.getString("Item Utils.No Number", locale).replaceAll("%arg%", args[0]));
                    }
                } else {
                    p.sendMessage(api.getString("Item Utils.No Item", locale));
                }
            } else {
                p.sendMessage(api.getString("Item Utils.RemoveLore.Help", locale));
            }
        } else {
            sender.sendMessage(api.getString("No Console", locale));
        }
    }
}
