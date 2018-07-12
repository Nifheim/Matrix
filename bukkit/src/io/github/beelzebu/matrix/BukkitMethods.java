package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.event.LevelUPEvent;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import io.github.beelzebu.matrix.interfaces.IMethods;
import io.github.beelzebu.matrix.player.BukkitMatrixPlayer;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.utils.BukkitMessages;
import io.github.beelzebu.matrix.utils.MessagesManager;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class BukkitMethods implements IMethods {

    private final Main plugin = Main.getInstance();
    private final CommandSender console = Bukkit.getConsoleSender();

    @Override
    public IConfiguration getConfig() {
        return plugin.getConfiguration();
    }

    @Override
    public MessagesManager getMessages(String lang) {
        return new BukkitMessages(lang);
    }

    @Override
    public void runAsync(Runnable rn) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, rn);
    }

    @Override
    public void runAsync(Runnable rn, Integer timer) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, rn, 0, timer * 20);
    }

    @Override
    public void runSync(Runnable rn) {
        Bukkit.getScheduler().runTask(plugin, rn);
    }

    @Override
    public void executeCommand(String cmd) {
        Bukkit.dispatchCommand(console, cmd);
    }

    @Override
    public void log(Object log) {
        console.sendMessage(MatrixAPI.getInstance().rep("&8[&cMatrix&8] &7" + log));
    }

    @Override
    public String getNick(UUID uuid) {
        return Bukkit.getPlayer(uuid).getName();
    }

    @Override
    public UUID getUUID(String player) {
        return Bukkit.getServer().getPlayer(player).getUniqueId();
    }

    @Override
    public Object getConsole() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public void sendMessage(Object sender, BaseComponent[] msg) {
        ((CommandSender) sender).sendMessage(msg);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public InputStream getResource(String file) {
        return plugin.getResource(file);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean isOnline(String name) {
        return isOnline(name, true);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return isOnline(uuid, true);
    }

    @Override
    public void callLevelUPEvent(UUID uuid, long newexp, long oldexp) {
        Bukkit.getPluginManager().callEvent(new LevelUPEvent(uuid, newexp, oldexp));
    }

    @Override
    public String getLocale(UUID uuid) {
        if (isOnline(uuid)) {
            return Bukkit.getPlayer(uuid).getLocale();
        }
        return "";
    }

    @Override
    public void ban(String name) {
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, "nope :p", null, null);
    }

    @Override
    public MatrixPlayer getPlayer(UUID uuid) {
        return new BukkitMatrixPlayer(uuid);
    }

    @Override
    public void sendMessage(String name, String message) {
        Bukkit.getPlayer(name).sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Bukkit.getPlayer(uuid).sendMessage(message);
    }

    @Override
    public boolean isOnline(String name, boolean here) {
        return Bukkit.getPlayer(name) != null && Bukkit.getPlayer(name).isOnline();
    }

    @Override
    public boolean isOnline(UUID uuid, boolean here) {
        return Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
}
