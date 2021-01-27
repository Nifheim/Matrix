package com.github.beelzebu.matrix.bungee.plugin;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.command.BungeeCommandSource;
import com.github.beelzebu.matrix.api.command.CommandSource;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bungee.config.BungeeConfiguration;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MatrixPluginBungee implements MatrixPlugin {

    private final MatrixBungeeBootstrap bootstrap;
    private final CommandSource console = new BungeeCommandSource(ProxyServer.getInstance().getConsole());
    private MatrixBungeeAPI api;

    public MatrixPluginBungee(MatrixBungeeBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public MatrixConfig getConfig() {
        return bootstrap.getConfig();
    }

    @Override
    public AbstractConfig getFileAsConfig(File file) {
        return new BungeeConfiguration(file);
    }

    @Override
    public void executeCommand(String cmd) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmd);
    }

    @Override
    public CommandSource getConsole() {
        return console;
    }

    @Override
    public void sendMessage(String name, String message) {
        ProxyServer.getInstance().getPlayer(name).sendMessage(TextComponent.fromLegacyText(StringUtils.replace(message)));
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        ProxyServer.getInstance().getPlayer(uuid).sendMessage(TextComponent.fromLegacyText(StringUtils.replace(message)));
    }

    @Override
    public File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    @Override
    public InputStream getResource(String filename) {
        return bootstrap.getResourceAsStream(filename);
    }

    @Override
    public String getVersion() {
        return bootstrap.getDescription().getVersion();
    }

    @Override
    public boolean isOnline(String name, boolean here) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(name);
        if (here) {
            return proxiedPlayer != null && proxiedPlayer.isConnected();
        } else {
            if (proxiedPlayer != null) {
                return proxiedPlayer.isConnected();
            }
            return api.getPlayerManager().isOnlineByName(name, null, null).join();
        }
    }

    @Override
    public boolean isOnline(UUID uniqueId, boolean here) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uniqueId);
        if (here) {
            return proxiedPlayer != null && proxiedPlayer.isConnected();
        } else {
            if (proxiedPlayer != null) {
                return proxiedPlayer.isConnected();
            }
            return api.getPlayerManager().isOnline(uniqueId, null, null).join();
        }
    }

    @Override
    public UUID getUniqueId(String name) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(name);
        if (proxiedPlayer != null) {
            return proxiedPlayer.getUniqueId();
        }
        return null;
    }

    @Override
    public void kickPlayer(UUID uniqueId, String reason) {
        Objects.requireNonNull(uniqueId, "UUID can't be null.");
        Objects.requireNonNull(reason, "Kick reason can't be null");
        if (isOnline(uniqueId, true)) {
            ProxyServer.getInstance().getPlayer(uniqueId).disconnect(TextComponent.fromLegacyText(reason));
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        Objects.requireNonNull(name, "Name can't be null.");
        Objects.requireNonNull(reason, "Kick reason can't be null");
        if (isOnline(name, true)) {
            ProxyServer.getInstance().getPlayer(name).disconnect(TextComponent.fromLegacyText(reason));
        }
    }

    @Override
    public void kickPlayer(MatrixPlayer matrixPlayer, String reason) {
        Objects.requireNonNull(matrixPlayer, "Player can't be null");
        kickPlayer(matrixPlayer.getName(), reason);
    }

    @Override
    public MatrixBungeeBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public void dispatchCommand(CommandSource commandSource, String command) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(commandSource.getName());
        if (proxiedPlayer != null && proxiedPlayer.isConnected()) {
            ProxyServer.getInstance().getPluginManager().dispatchCommand(proxiedPlayer, command);
        }
    }

    @Override
    public CompletableFuture<Collection<MatrixPlayer>> getLoggedInPlayers() {
        return CompletableFuture.supplyAsync(() -> ProxyServer.getInstance().getPlayers().stream().map(proxiedPlayer -> api.getPlayerManager().getPlayer(proxiedPlayer).join()).collect(Collectors.toSet()), bootstrap.getScheduler().async());
    }

    @Override
    public Optional<String> getHexId(UUID uniqueId) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uniqueId);
        if (proxiedPlayer != null) {
            return Optional.ofNullable(api.getMetaInjector().getId(proxiedPlayer));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHexId(String name) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(name);
        if (proxiedPlayer != null) {
            return Optional.ofNullable(api.getMetaInjector().getId(proxiedPlayer));
        }
        return Optional.empty();
    }

    public String toString() {
        return "MatrixPluginBungee(bootstrap=" + bootstrap + ", console=" + getConsole() + ")";
    }

    public MatrixBungeeAPI getApi() {
        return api;
    }

    public void setApi(MatrixBungeeAPI api) {
        this.api = api;
    }
}
