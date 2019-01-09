package io.github.beelzebu.matrix.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.utils.ReadURL;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI api = Matrix.getAPI();
    private boolean firstjoin = true;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (api.getPlayer(e.getUniqueId()) == null) { // si el usuario aún no existe en la base de datos es porque no ha entrado por el proxy
            e.disallow(Result.KICK_BANNED, "Alto ahí rufián, no todo es diversión, estás entrando de forma no autorizada.");
            return;
        }
        if (!api.getPlayer(e.getName()).getUniqueId().equals(e.getUniqueId())) {
            e.disallow(Result.KICK_OTHER, "Tu UUID no coincide con la UUID que hay en nuestra base de datos\ntus datos fueron registrados por seguridad.");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ServerType type = api.getServerInfo().getServerType();
        e.setJoinMessage(null);
        if ((type.equals(ServerType.LOBBY) || type.equals(ServerType.SURVIVAL))) {
            if (!p.hasPermission("matrix.staff")) {
                if (p.hasPermission("matrix.joinmessage")) {
                    e.setJoinMessage(api.rep(" &8[&a+&8] &f" + api.getPlayer(p.getUniqueId()).getDisplayName() + " &ese ha unido al servidor"));
                }
                Bukkit.getOnlinePlayers().forEach(op -> op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 2));
            }
        }
        MatrixPlayer matrixPlayer = api.getPlayer(p.getUniqueId());
        // Async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isVotifier()) {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + plugin.getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(PlayerJoinListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", p.getName());
                }
            }
        });
        api.getPlugin().runSync(() -> api.getPlayer(p.getUniqueId()).getOptions().forEach(opt -> api.getPlayer(p.getUniqueId()).setOption(opt, true)));

        // Later task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // For security
            if (p.isOp()) {
                p.setOp(false);
            }
            if (firstjoin) {
                plugin.getConfig().getStringList("Join cmds").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                firstjoin = false;
            }
            if (!p.hasPermission("matrix.staff.mod") && !p.hasPermission("matrix.vip.earl") && !p.hasPermission("matrix.vip.count")) {
                matrixPlayer.setOption(PlayerOptionType.FLY, false);
            }
            if (api.getConfig().getBoolean("News")) { // TODO: mejor manejo de múltiples páginas y editar nombre del servidor
                ItemStack old = p.getInventory().getItemInOffHand();
                p.getInventory().setItemInOffHand(book("Noticias Nifheim", "Nifheim Network", (List<List<String>>) api.getConfig().getList("News Lines")));
                try {
                    PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
                    pc.getModifier().writeDefaults();
                    ByteBuf bf = Unpooled.buffer(256);
                    bf.setByte(0, (byte) 1);
                    bf.writerIndex(1);
                    pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
                    pc.getStrings().write(0, "MC|BOpen"); // TODO: testear en 1.13 por los cambios en canales de mensajería
                    ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
                } catch (FieldAccessException | InvocationTargetException ignore) {
                }
                if (old != null) {
                    p.getInventory().setItemInOffHand(old);
                } else {
                    p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
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
