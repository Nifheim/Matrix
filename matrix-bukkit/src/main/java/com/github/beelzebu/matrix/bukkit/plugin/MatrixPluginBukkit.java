package com.github.beelzebu.matrix.bukkit.plugin;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.command.BukkitCommandSource;
import com.github.beelzebu.matrix.api.command.CommandSource;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bukkit.config.BukkitConfiguration;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class MatrixPluginBukkit implements MatrixPlugin {

    private final MatrixBukkitBootstrap bootstrap;
    private final CommandSource console = new BukkitCommandSource(Bukkit.getConsoleSender());
    private MatrixBukkitAPI api;

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
    public void executeCommand(String cmd) {
        bootstrap.getScheduler().executeSync(() -> console.execute(cmd));
    }

    @Override
    public CommandSource getConsole() {
        return console;
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

    public MatrixBukkitBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public void dispatchCommand(CommandSource commandSource, String command) {
        Player player = Bukkit.getPlayer(commandSource.getName());
        if (player != null && player.isOnline()) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    @Override
    public CompletableFuture<Collection<MatrixPlayer>> getLoggedInPlayers() {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOnlinePlayers().stream().map(player -> api.getPlayerManager().getPlayer(player).join()).collect(Collectors.toSet()), bootstrap.getScheduler().async());
    }

    @Override
    public Optional<String> getHexId(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            return Optional.ofNullable(api.getMetaInjector().getId(player));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHexId(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            return Optional.ofNullable(api.getMetaInjector().getId(player));
        }
        return Optional.empty();
    }

    public String toString() {
        return "MatrixPluginBukkit(bootstrap=" + bootstrap + ", console=" + getConsole() + ")";
    }

    public MatrixBukkitAPI getApi() {
        return api;
    }

    public void setApi(MatrixBukkitAPI api) {
        this.api = api;
    }
}
