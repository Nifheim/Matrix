package com.github.beelzebu.matrix.command.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.github.beelzebu.matrix.api.commands.MatrixCommand;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.listener.lobby.LobbyListener;
import com.github.beelzebu.matrix.menus.ProfileGUI;
import com.github.beelzebu.matrix.util.LocationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
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
        super("matrix", "matrix.mod");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("sound")) {
            _sound(sender, args);
        } else if (args[0].equalsIgnoreCase("setspawn")) {
            _setspawn(sender, args);
        } else if (args[0].equalsIgnoreCase("newsbook")) {
            _book(sender, args);
        } else if (args[0].equalsIgnoreCase("profile")) {
            new ProfileGUI(((Player) sender), api.getString("Social.Profile.Name", ((Player) sender).getLocale())).open((Player) sender);
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
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.valueOf(args[1].toUpperCase()), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
            }
        }
        return true;
    }

    private boolean _setspawn(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Location loc = ((Entity) sender).getLocation();
            loc.getWorld().setSpawnLocation(((Player) sender).getLocation());
            data.getConfig().set("spawn", LocationUtils.locationToString(loc));
            data.saveConfig();
        }
        return true;
    }

    private boolean _book(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ItemStack old = p.getInventory().getItemInOffHand();
            p.getInventory().setItemInOffHand(book("Noticias", "Network", (List<List<String>>) api.getConfig().getList("News Lines")));
            try {
                PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
                pc.getModifier().writeDefaults();
                ByteBuf bf = Unpooled.buffer(256);
                bf.setByte(0, (byte) 1);
                bf.writerIndex(1);
                pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
                pc.getStrings().write(0, "MC|BOpen");
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
            } catch (FieldAccessException | InvocationTargetException ex) {
            }
            if (old != null) {
                p.getInventory().setItemInOffHand(old);
            } else {
                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
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
