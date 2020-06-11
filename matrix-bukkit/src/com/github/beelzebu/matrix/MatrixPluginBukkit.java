package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.BukkitCommandSource;
import com.github.beelzebu.matrix.api.command.CommandSource;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.config.BukkitConfiguration;
import com.github.beelzebu.matrix.util.bungee.PluginMessage;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class MatrixPluginBukkit implements MatrixPlugin {

    private final MatrixBukkitBootstrap bootstrap;
    private final CommandSource console = new BukkitCommandSource(Bukkit.getConsoleSender());

    public MatrixPluginBukkit(MatrixBukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public MatrixConfig getConfig() {
        return bootstrap.getConfiguration();
    }

    @Override
    public AbstractConfig getFileAsConfig(File file) {
        return new BukkitConfiguration(file);
    }

    @Override
    public void runAsync(Runnable rn) {
        Bukkit.getScheduler().runTaskAsynchronously(bootstrap, rn);
    }

    @Override
    public void runAsync(Runnable rn, Integer timer) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(bootstrap, rn, 0, timer * 20);
    }

    @Override
    public void runSync(Runnable rn) {
        Bukkit.getScheduler().runTask(bootstrap, rn);
    }

    @Override
    public void executeCommand(String cmd) {
        runSync(() -> console.execute(cmd));
    }

    @Override
    public CommandSource getConsole() {
        return console;
    }

    @Override
    public void sendMessage(Object sender, BaseComponent[] msg) {
        if (sender instanceof CommandSender) {
            ((CommandSender) sender).sendMessage(msg);
        } else if (sender instanceof CommandSource) {
            ((CommandSource) sender).sendMessage(TextComponent.toLegacyText(msg));
        }
    }

    @Override
    public void sendMessage(String name, String message) {
        Bukkit.getPlayer(name).sendMessage(StringUtils.replace(message));
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Bukkit.getPlayer(uuid).sendMessage(StringUtils.replace(message));
    }

    @Override
    public File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    @Override
    public InputStream getResource(String file) {
        return bootstrap.getResource(file);
    }

    @Override
    public String getVersion() {
        return bootstrap.getDescription().getVersion();
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
    public String getLocale(UUID uuid) {
        if (isOnline(uuid, true)) {
            return Bukkit.getPlayer(uuid).getLocale();
        }
        return "";
    }

    @Override
    public void ban(String name) {
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, "nope :p", null, null);
    }

    @Override
    public UUID getUniqueId(String name) {
        return isOnline(name, true) ? Bukkit.getPlayer(name).getUniqueId() : null;
    }

    @Override
    public void kickPlayer(UUID uniqueId, String reason) {
        Objects.requireNonNull(uniqueId, "UUID can't be null.");
        Objects.requireNonNull(reason, "Kick reason can't be null");
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null && player.isOnline()) {
            player.sendMessage(reason);
            if (getConfig().getLobby() != null) {
                PluginMessage.get().sendMessage("BungeeCord", "Connect", Collections.singletonList(getConfig().getLobby()), player);
            }
            player.kickPlayer(reason);
        } else {
            Matrix.getLogger().debug("Tried to kick " + uniqueId + ", but isn't online.");
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        Objects.requireNonNull(name, "Name can't be null.");
        Objects.requireNonNull(reason, "Kick reason can't be null");
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.isOnline()) {
            player.sendMessage(reason);
            if (getConfig().getLobby() != null) {
                PluginMessage.get().sendMessage("BungeeCord", "Connect", Collections.singletonList(getConfig().getLobby()), player);
            }
            player.kickPlayer(reason);
        } else {
            Matrix.getLogger().debug("Tried to kick " + name + ", but isn't online.");
        }
    }

    @Override
    public void kickPlayer(MatrixPlayer matrixPlayer, String reason) {
        Objects.requireNonNull(matrixPlayer, "Player can't be null");
        kickPlayer(matrixPlayer.getName(), reason);
    }

    @Override
    public void dispatchCommand(CommandSource commandSource, String command) {
        Player player = Bukkit.getPlayer(commandSource.getName());
        if (player != null && player.isOnline()) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    public MatrixBukkitBootstrap getBootstrap() {
        return bootstrap;
    }

    public String toString() {
        return "MatrixPluginBukkit(bootstrap=" + bootstrap + ", console=" + getConsole() + ")";
    }
}