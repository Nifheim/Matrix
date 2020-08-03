package com.github.beelzebu.matrix.command.utils;

import cl.indiopikaro.jmatrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.listener.lobby.LobbyListener;
import com.github.beelzebu.matrix.menus.ProfileGUI;
import com.github.beelzebu.matrix.util.CompatUtil;
import com.github.beelzebu.matrix.util.LocationUtils;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author Beelzebu
 */
public class MatrixManagerCommand extends MatrixCommand {

    private final LobbyData data = LobbyData.getInstance();

    public MatrixManagerCommand() {
        super("matrix", "matrix.command.matrix");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("sound")) {
            _sound(sender, args);
        } else if (args[0].equalsIgnoreCase("setspawn")) {
            _setspawn(sender, args);
        } else if (args[0].equalsIgnoreCase("profile")) {
            new ProfileGUI(api.getPlayer(((Player) sender).getUniqueId())).open((Player) sender);
        } else if (args[0].equalsIgnoreCase("editmode")) {
            if (sender.hasPermission("matrix.staff.admin") && sender instanceof Player && api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                LobbyListener.getEditMode().add((Player) sender);
            }
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

    private boolean _setspawn(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Location loc = ((Entity) sender).getLocation();
            data.getConfig().set("spawn", LocationUtils.locationToString(loc));
            data.saveConfig();
            if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
                loc.getWorld().setSpawnLocation(((Player) sender).getLocation());
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
