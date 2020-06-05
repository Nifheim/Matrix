package com.github.beelzebu.matrix.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.github.beelzebu.matrix.util.PermsUtils;
import com.github.beelzebu.matrix.util.ReadURL;
import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author Beelzebu
 */
public class PlayerJoinListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI api = Matrix.getAPI();
    private final Set<UUID> premiumPlayer = new HashSet<>();
    private boolean firstjoin = true;

    public PlayerJoinListener(MatrixBukkitBootstrap plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        // TODO: database log
        // mysql log?
        MatrixPlayer matrixPlayer = api.getPlayer(e.getUniqueId());
        if (matrixPlayer == null) { // si el usuario aún no existe en la base de datos es porque no ha entrado por el proxy
            e.disallow(Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            return;
        }
        if (!matrixPlayer.isLoggedIn() && !api.getServerInfo().getGroupName().equals("auth")) {
            if (matrixPlayer.isPremium() && api.getServerInfo().getGroupName().equals("lobby")) {
                premiumPlayer.add(e.getUniqueId());
                return;
            }
            e.disallow(Result.KICK_BANNED, "Se ha detectado un acceso no autorizado.");
            return;
        }
        if (!api.getPlayer(e.getName()).getUniqueId().equals(e.getUniqueId())) {
            e.disallow(Result.KICK_OTHER, "Tu UUID no coincide con la UUID que hay en nuestra base de datos\ntus datos fueron registrados por seguridad.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        if (premiumPlayer.contains(player.getUniqueId())) {
            premiumPlayer.remove(player.getUniqueId());
            player.kickPlayer("Se ha detectado un acceso no autorizado.");
            return;
        }
        ServerType type = api.getServerInfo().getServerType();
        if ((type.equals(ServerType.LOBBY) || type.equals(ServerType.SURVIVAL))) {
            if (!player.hasPermission("matrix.mod")) {
                if (player.hasPermission("matrix.joinmessage")) {
                    e.setJoinMessage(StringUtils.replace(" &8[&a+&8] &f" + PermsUtils.getPrefix(player.getUniqueId()) + api.getPlayer(player.getUniqueId()).getDisplayName() + " &ese ha unido al servidor"));
                }
                Bukkit.getOnlinePlayers().forEach(op -> op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 2));
            }
        }
        MatrixPlayer matrixPlayer = api.getPlayer(player.getUniqueId());
        // Async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isVotifier()) {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + player.getName() + "&clave=" + plugin.getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(PlayerJoinListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", player.getName());
                }
            }
        });

        // Later task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // For security
            if (player.isOp()) {
                player.setOp(false);
            }
            if (firstjoin) {
                plugin.getConfig().getStringList("Join cmds").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                firstjoin = false;
            }
            if (!player.hasPermission("matrix.mod") && !player.hasPermission("matrix.command.fly")) {
                matrixPlayer.setOption(PlayerOptionType.FLY, false);
            }
            if (api.getConfig().getBoolean("News")) { // TODO: mejor manejo de múltiples páginas y editar nombre del servidor
                ItemStack old = player.getInventory().getItemInOffHand();
                player.getInventory().setItemInOffHand(book("Noticias Nifheim", "Nifheim Network", (List<List<String>>) api.getConfig().getList("News Lines")));
                try {
                    PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
                    pc.getModifier().writeDefaults();
                    ByteBuf bf = Unpooled.buffer(256);
                    bf.setByte(0, (byte) 1);
                    bf.writerIndex(1);
                    pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
                    pc.getStrings().write(0, "MC|BOpen"); // TODO: testear en 1.13 por los cambios en canales de mensajería
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, pc);
                } catch (FieldAccessException | InvocationTargetException ignore) {
                }
                if (old != null) {
                    player.getInventory().setItemInOffHand(old);
                } else {
                    player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            }
        }, 6);
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
