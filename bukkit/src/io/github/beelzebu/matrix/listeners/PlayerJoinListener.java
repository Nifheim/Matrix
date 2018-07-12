package io.github.beelzebu.matrix.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import io.github.beelzebu.matrix.utils.ReadURL;
import io.github.beelzebu.matrix.utils.ServerType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class PlayerJoinListener implements Listener {

    private static boolean firstjoin = true;
    private final Main plugin;
    private final MatrixAPI core = MatrixAPI.getInstance();

    public PlayerJoinListener(Main main) {
        plugin = main;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (core.getUUID(e.getName(), true) != null && !core.getUUID(e.getName(), true).equals(e.getUniqueId())) {
            e.disallow(Result.KICK_OTHER, "Tu UUID no coincide con la UUID que hay en nuestra base de datos");
            return;
        }
        core.getMethods().runAsync(() -> {
            if (e.getLoginResult().equals(Result.ALLOWED)) {
                if (!core.getRedis().isRegistred(e.getUniqueId())) {
                    core.getRedis().createPlayer(e.getUniqueId(), e.getName());
                }
                core.getPlayer(e.getUniqueId());
                core.getRedis().setData(e.getUniqueId(), "ip", e.getAddress().getHostAddress());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ServerType type = core.getServerInfo().getServerType();
        e.setJoinMessage(null);
        if ((type.equals(ServerType.LOBBY) || type.equals(ServerType.SURVIVAL))) {
            if (!p.hasPermission("matrix.staff")) {
                if (p.hasPermission("matrix.vip")) {
                    e.setJoinMessage(core.rep(" &8[&a+&8] &f" + core.getDisplayName(p.getUniqueId(), true) + " &ese ha unido al servidor"));
                }
                Bukkit.getOnlinePlayers().forEach(op -> op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 2));
            }
        }
        MatrixPlayer np = core.getPlayer(p.getUniqueId());
        // Async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isVotifier()) {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + plugin.getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(PlayerJoinListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", p.getName());
                }
            }
            if (type.equals(ServerType.SURVIVAL)) {
                if (!core.getRedis().isRegistred(p.getUniqueId(), core.getServerInfo().getServerName())) {
                    core.getRedis().saveStats(p.getUniqueId(), core.getServerInfo().getServerName(), null);
                }
                // Add the player to the stats maps
                try (Jedis jedis = core.getRedis().getPool().getResource()) {
                    JsonObject userdata_server = core.getGson().fromJson(jedis.hget("ncore_" + core.getServerInfo().getServerName() + "_stats", p.getUniqueId().toString()), JsonObject.class);
                    if (!StatsListener.getPlaced().containsKey(p)) {
                        StatsListener.getPlaced().put(p, userdata_server.get("placed").getAsInt());
                    }
                    if (!StatsListener.getBroken().containsKey(p)) {
                        StatsListener.getBroken().put(p, userdata_server.get("broken").getAsInt());
                    }
                }
            }
        });
        core.getMethods().runSync(() -> core.getPlayer(p.getUniqueId()).getActiveOptions().forEach(opt -> core.getPlayer(p.getUniqueId()).setOption(opt, true)));

        // Later task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // For security
            if (p.isOp()) {
                p.setOp(false);
            }
            if (firstjoin) {
                plugin.getConfig().getStringList("Join cmds").forEach((cmd) -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                });
                firstjoin = false;
            }
            if (!p.hasPermission("matrix.staff.mod") && !p.hasPermission("matrix.vip.earl") && !p.hasPermission("matrix.vip.count")) {
                np.setOption(PlayerOptionType.FLY, false);
            }
            if (core.getConfig().getBoolean("News")) {
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
        }, 6);
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
