package com.github.beelzebu.matrix.bukkit.plugin;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.command.BukkitCommandSource;
import com.github.beelzebu.matrix.api.command.CommandSource;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bukkit.config.BukkitConfiguration;
import com.github.beelzebu.matrix.config.MatrixConfiguration;
import com.github.beelzebu.matrix.plugin.MatrixPluginCommon;
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
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class MatrixPluginBukkit implements MatrixPluginCommon {

    private final MatrixBukkitBootstrap bootstrap;
    private final CommandSource console = new BukkitCommandSource(Bukkit.getConsoleSender());
    private MatrixBukkitAPI api;

    public MatrixPluginBukkit(MatrixBukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public @NotNull BukkitConfiguration getConfig() {
        return bootstrap.getConfiguration();
    }

    @Override
    public @NotNull AbstractConfig getFileAsConfig(@NotNull File file) {
        return new BukkitConfiguration(file);
    }

    @Override
    public void executeCommand(String cmd) {
        bootstrap.getScheduler().executeSync(() -> console.execute(cmd));
    }

    @Override
    public @NotNull CommandSource getConsole() {
        return console;
    }

    @Override
    public void sendMessage(@NotNull String name, @NotNull String message) {
        Bukkit.getPlayer(name).sendMessage(StringUtils.replace(message));
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String message) {
        Bukkit.getPlayer(uuid).sendMessage(StringUtils.replace(message));
    }

    @Override
    public @NotNull File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    @Override
    public @NotNull InputStream getResource(@NotNull String file) {
        return bootstrap.getResource(file);
    }

    @Override
    public @NotNull String getVersion() {
        return bootstrap.getDescription().getVersion();
    }

    @Override
    public boolean isOnline(@NotNull String name, boolean here) {
        boolean foundHere = Bukkit.getPlayer(name) != null && Bukkit.getPlayer(name).isOnline();
        if (foundHere) {
            return true;
        } else if (here) {
            return false;
        }
        return Matrix.getAPI().getPlayerManager().isOnlineByName(name, null, null).join();
    }

    @Override
    public boolean isOnline(@NotNull UUID uuid, boolean here) {
        boolean foundHere = Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline();
        if (foundHere) {
            return true;
        } else if (here) {
            return false;
        }
        return Matrix.getAPI().getPlayerManager().isOnline(uuid, null, null).join();
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull String name) {
        return isOnline(name, true) ? Bukkit.getPlayer(name).getUniqueId() : null;
    }

    @Override
    public void kickPlayer(@NotNull UUID uniqueId, @NotNull String reason) {
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
    public void kickPlayer(@NotNull String name, @NotNull String reason) {
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
    public void kickPlayer(@NotNull MatrixPlayer matrixPlayer, @NotNull String reason) {
        Objects.requireNonNull(matrixPlayer, "Player can't be null");
        kickPlayer(matrixPlayer.getName(), reason);
    }

    public @NotNull MatrixBukkitBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public void dispatchCommand(@NotNull CommandSource commandSource, @NotNull String command) {
        Player player = Bukkit.getPlayer(commandSource.getName());
        if (player != null && player.isOnline()) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    @Override
    public @NotNull CompletableFuture<Collection<MatrixPlayer>> getLoggedInPlayers() {
        return bootstrap.getScheduler().makeFuture(() -> Bukkit.getOnlinePlayers().stream().map(player -> api.getPlayerManager().getPlayer(player).join()).collect(Collectors.toSet()));
    }

    @Override
    public @NotNull Optional<String> getHexId(@NotNull UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            return Optional.ofNullable(api.getPlayerManager().getMetaInjector().getId(player));
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getHexId(@NotNull String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            return Optional.ofNullable(api.getPlayerManager().getMetaInjector().getId(player));
        }
        return Optional.empty();
    }

    public @NotNull String toString() {
        return "MatrixPluginBukkit(bootstrap=" + bootstrap + ", console=" + getConsole() + ")";
    }

    public MatrixBukkitAPI getApi() {
        return api;
    }

    public void setApi(MatrixBukkitAPI api) {
        this.api = api;
    }

    @Override
    public MatrixConfiguration getMatrixConfiguration() {
        return getConfig();
    }
}
