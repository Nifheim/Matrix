package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.command.BukkitCommandSource;
import io.github.beelzebu.matrix.api.command.CommandSource;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.config.BukkitConfiguration;
import io.github.beelzebu.matrix.event.LevelUPEvent;
import io.github.beelzebu.matrix.utils.bungee.PluginMessage;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import lombok.Data;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
@Data
public class MatrixPluginBukkit implements MatrixPlugin {

    private final MatrixBukkitBootstrap bootstrap;
    private final CommandSource console = new BukkitCommandSource(Bukkit.getConsoleSender());

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
        Bukkit.getPlayer(name).sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Bukkit.getPlayer(uuid).sendMessage(message);
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
    public void callLevelUPEvent(UUID uuid, long newexp, long oldexp) {
        Bukkit.getPluginManager().callEvent(new LevelUPEvent(uuid, newexp, oldexp));
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
            PluginMessage.get().sendMessage("BungeeCord", "Connect", Collections.singletonList(getConfig().getLobby()), player);
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
            PluginMessage.get().sendMessage("BungeeCord", "Connect", Collections.singletonList(getConfig().getLobby()), player);
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
}
