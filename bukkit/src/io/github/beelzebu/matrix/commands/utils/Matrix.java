package io.github.beelzebu.matrix.commands.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.listeners.LobbyListener;
import io.github.beelzebu.matrix.menus.ProfileGUI;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import io.github.beelzebu.matrix.server.lobby.LobbyData;
import io.github.beelzebu.matrix.utils.ServerType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.bukkit.Bukkit;
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
public class Matrix extends MatrixCommand {

    private final LobbyData data = LobbyData.getInstance();

    public Matrix() {
        super("nifheim", "matrix.staff.mod");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("xp")) {
            _xp(sender, args);
        } else if (args[0].equalsIgnoreCase("sound")) {
            _sound(sender, args);
        } else if (args[0].equalsIgnoreCase("setspawn")) {
            _setspawn(sender, args);
        } else if (args[0].equalsIgnoreCase("newsbook")) {
            _book(sender, args);
        } else if (args[0].equalsIgnoreCase("profile")) {
            new ProfileGUI(((Player) sender), core.getString("Social.Profile.Name", ((Player) sender).getLocale())).open((Player) sender);
        } else if (args[0].equalsIgnoreCase("editmode")) {
            if (sender.hasPermission("matrix.staff.admin") && sender instanceof Player && core.getPlayer(((Player) sender).getUniqueId()).isInLobby()) {
                LobbyListener.getEditMode().add((Player) sender);
            }
        }
    }

    private boolean _xp(CommandSender sender, String[] args) {
        String locale = "";
        if (sender instanceof Player) {
            locale = ((Player) sender).getLocale();
        }
        // (0)xp (1)set (2)player (3)int = 4
        if (args.length == 1 || args[1].equalsIgnoreCase("?")) {
            core.getMessages(locale).getStringList("NetworkXP.Help.User").forEach((str) -> {
                sender.sendMessage(core.rep(str));
            });
            return true;
        } else if (args[1].equalsIgnoreCase("admin")) {
            core.getMessages(locale).getStringList("NetworkXP.Help.Admin").forEach((str) -> {
                sender.sendMessage(core.rep(str));
            });
            return true;
        } else if (args[1].equalsIgnoreCase("get") && args.length <= 3) {
            switch (args.length) {
                case 2:
                    if (sender instanceof Player) {
                        long xp = NetworkXP.MCEXP.getXPForPlayer(sender.getName());
                        int level = NetworkXP.getLevelForXP(xp);
                        long nextxp = NetworkXP.getXPForLevel(level + 1) - xp;
                        sender.sendMessage(core.getString("NetworkXP.Get.Self", locale).replaceAll("%player%", sender.getName()).replaceAll("%level%", String.valueOf(level)).replaceAll("%xp%", String.valueOf(nextxp)));
                    } else {
                        sender.sendMessage(core.getString("No Console", locale));
                    }
                    break;
                case 3:
                    if (args[2] != null && core.getRedis().isRegistred(args[2])) {
                        long xp = NetworkXP.MCEXP.getXPForPlayer(args[2]);
                        int level = NetworkXP.getLevelForXP(xp);
                        long nextxp = NetworkXP.getXPForLevel(level + 1) - xp;
                        sender.sendMessage(core.getString("NetworkXP.Get.Target", locale).replaceAll("%player%", args[2]).replaceAll("%level%", String.valueOf(level)).replaceAll("%xp%", String.valueOf(nextxp)));
                    } else {
                        sender.sendMessage(core.getString("NetworkXP.Get.No Target", locale));
                    }
                    break;
                default:
                    break;
            }
        } else if (args.length == 4 && args[1].matches("(^give$|^add$)")) {
            if (args[2] == null || args[2].equalsIgnoreCase("?")) {
            } else if (args[2] != null && core.getRedis().isRegistred(args[2])) {
                long xp = Long.parseLong(args[3]);
                int level;
                long xp_final;
                if (args[3].endsWith("L") || args[3].endsWith("l")) {
                    xp = NetworkXP.getXPForLevel(Integer.parseInt(args[3].replaceAll("L", "").replaceAll("l", "")));
                }
                NetworkXP.addXPForPlayer(core.getUUID(args[2]), xp);
                level = NetworkXP.getLevelForXP(xp + NetworkXP.getXPForPlayer(core.getUUID(args[2]))) - NetworkXP.getLevelForPlayer(core.getUUID(args[2]));
                xp_final = NetworkXP.getXPForLevel(level) > 0 ? NetworkXP.getXPForLevel(level) - xp : xp;
                sender.sendMessage(core.getString("NetworkXP.Add.Sender", locale).replaceAll("%player%", args[2]).replaceAll("%level%", String.valueOf(level)).replaceAll("%xp%", String.valueOf(xp_final)));
                Player target = Bukkit.getPlayer(args[2]);
                if (target != null) {
                    target.sendMessage(core.getString("NetworkXP.Add.Target", locale).replaceAll("%level%", String.valueOf(level)).replaceAll("%xp%", String.valueOf(xp_final)));
                    if (core.getServerInfo().getServerType().equals(ServerType.LOBBY) || core.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA)) {
                        level = NetworkXP.getLevelForPlayer(target.getUniqueId());
                        xp = (int) (NetworkXP.MCEXP.getXPForPlayer(target.getName()) - NetworkXP.MCEXP.getXPForLevel(level));
                        target.setLevel(0);
                        target.setExp(0);
                        target.setLevel(level);
                        target.giveExp((int) xp);
                    }
                }
            }
        }
        return true;
    }

    private boolean _sound(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (Sound.valueOf(args[1]) != null && args[2] != null && args[3] != null) {
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
            data.getConfig().set("Spawn.World", loc.getWorld().getName());
            data.getConfig().set("Spawn.Location", loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
            data.saveConfig();
        }
        return true;
    }

    private boolean _book(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ItemStack old = p.getInventory().getItemInOffHand();
            p.getInventory().setItemInOffHand(book("Noticias Nifheim", "Nifheim Network", (List<List<String>>) core.getConfig().getList("News Lines")));
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
        pages.forEach(page -> {
            meta.addPage(page.toArray(new String[page.size()]));
        });
        is.setItemMeta(meta);
        return is;
    }
}
