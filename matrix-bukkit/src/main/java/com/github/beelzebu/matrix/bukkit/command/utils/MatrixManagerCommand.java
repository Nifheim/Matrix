package com.github.beelzebu.matrix.bukkit.command.utils;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author Beelzebu
 */
public class MatrixManagerCommand extends MatrixCommand {


    public MatrixManagerCommand() {
        super("matrix", "matrix.command.matrix");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("sound")) {
            _sound(sender, args);
        }
    }

    private boolean _sound(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (args[2] != null && args[3] != null) {
                if (args[1].contains(".")) {
                    args[1] = args[1].replaceAll("\\.", "_");
                }
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.valueOf(args[1].toUpperCase()), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            }
        }
        return true;
    }

    private ItemStack book(String title, String author, List<List<String>> pages) {
        ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) is.getItemMeta();
        meta.setAuthor(author);
        meta.setTitle(title);
        pages.forEach(page -> meta.addPage(page.toArray(new String[0])));
        is.setItemMeta(meta);
        return is;
    }
}
