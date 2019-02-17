package io.github.beelzebu.matrix;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.command.BungeeCommandSource;
import io.github.beelzebu.matrix.api.command.CommandSource;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.config.BungeeConfiguration;
import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.NonNull;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Data
public class MatrixPluginBungee implements MatrixPlugin {

    private final MatrixBungeeBootstrap bootstrap;
    private final CommandSource console = new BungeeCommandSource(ProxyServer.getInstance().getConsole());

    @Override
    public MatrixConfig getConfig() {
        return bootstrap.getConfig();
    }

    @Override
    public AbstractConfig getFileAsConfig(File file) {
        return new BungeeConfiguration(file);
    }

    @Override
    public void runAsync(Runnable rn) {
        ProxyServer.getInstance().getScheduler().runAsync(bootstrap, rn);
    }

    @Override
    public void runAsync(Runnable rn, Integer timer) {
        ProxyServer.getInstance().getScheduler().schedule(bootstrap, rn, 0, timer, TimeUnit.SECONDS);
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
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
    public void sendMessage(Object sender, BaseComponent[] msg) {
        if (sender instanceof CommandSender) {
            ((CommandSender) sender).sendMessage(msg);
        } else {
            Matrix.getLogger().debug(new IllegalArgumentException("Can't cast " + sender.getClass() + " to CommandSender"));
        }
    }

    @Override
    public void sendMessage(String name, String message) {
        ProxyServer.getInstance().getPlayer(name).sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        ProxyServer.getInstance().getPlayer(uuid).sendMessage(TextComponent.fromLegacyText(message));
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
    public boolean isOnline(@NonNull String name, boolean here) {
        if (!here) {
            return RedisBungee.getApi().isPlayerOnline(RedisBungee.getApi().getUuidFromName(name));
        } else {
            return ProxyServer.getInstance().getPlayer(name) != null;
        }
    }

    @Override
    public boolean isOnline(UUID uuid, boolean here) {
        if (!here) {
            return RedisBungee.getApi().isPlayerOnline(uuid);
        } else {
            return ProxyServer.getInstance().getPlayer(uuid) != null;
        }
    }

    @Override
    public void callLevelUPEvent(UUID uuid, long newexp, long oldexp) {
        throw new UnsupportedOperationException("callLevelUPEvent is not finished yet.");
    }

    @Override
    public String getLocale(UUID uuid) {
        if (ProxyServer.getInstance().getPlayer(uuid) != null) {
            return ProxyServer.getInstance().getPlayer(uuid).getLocale().getLanguage();
        }
        return "";
    }

    @Override
    public void ban(String name) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "ban " + name + " nope :p");
    }

    @Override
    public UUID getUniqueId(String name) {
        return isOnline(name, false) ? RedisBungee.getApi().getUuidFromName(name) : null;
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
    public void dispatchCommand(CommandSource commandSource, String command) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(commandSource.getName());
        if (proxiedPlayer != null && proxiedPlayer.isConnected()) {
            ProxyServer.getInstance().getPluginManager().dispatchCommand(proxiedPlayer, command);
        }
    }
}
