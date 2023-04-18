package com.github.beelzebu.matrix.bukkit.command.utils;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.command.BukkitCommandSource;
import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class MatrixManagerCommand extends MatrixCommand {


    public MatrixManagerCommand() {
        super("matrix", "matrix.command.matrix", false);
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (args[0].equalsIgnoreCase("sound")) {
            _sound(sender, args);
        }
        if (args[0].equalsIgnoreCase("motd")) {
            ((MatrixBukkitAPI) Matrix.getAPI()).motd(new BukkitCommandSource(sender));
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

    private @NotNull ItemStack book(String title, String author, @NotNull List<List<String>> pages) {
        ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) is.getItemMeta();
        meta.setAuthor(author);
        meta.setTitle(title);
        pages.forEach(page -> meta.addPage(page.toArray(new String[0])));
        is.setItemMeta(meta);
        return is;
    }
}
