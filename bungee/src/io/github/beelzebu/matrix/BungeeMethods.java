package io.github.beelzebu.matrix;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import io.github.beelzebu.matrix.interfaces.IMethods;
import io.github.beelzebu.matrix.player.BungeeMatrixPlayer;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.utils.MessagesManager;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

*

@author Beelzebu
        */

public class BungeeMethods implements IMethods {

    private final Main plugin = Main.getInstance();

    @Override
    public IConfiguration getConfig() {
        return plugin.getConfig();
    }

    @Override
    public MessagesManager getMessages(String lang) {
        throw new UnsupportedOperationException("getMessages is not finished yet.");
    }

    @Override
    public void runAsync(Runnable rn) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, rn);
    }

    @Override
    public void runAsync(Runnable rn, Integer timer) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, rn, 0, timer, TimeUnit.SECONDS);
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
    }

    @Override
    public void executeCommand(String cmd) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(plugin.getConsole(), cmd);
    }

    @Override
    public void log(Object log) {
        plugin.getConsole().sendMessage("§8[§cMatrix§8] §7" + log.toString());
    }

    @Override
    public String getNick(UUID uuid) {
        return RedisBungee.getApi().getNameFromUuid(uuid);
    }

    @Override
    public UUID getUUID(String player) {
        return RedisBungee.getApi().getUuidFromName(player);
    }

    @Override
    public Object getConsole() {
        return plugin.getConsole();
    }

    @Override
    public void sendMessage(Object sender, BaseComponent[] msg) {
        ((CommandSender) sender).sendMessage(msg);
    }

    @Override
    public File getDataFolder() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("Matrix").getDataFolder();
    }

    @Override
    public InputStream getResource(String filename) {
        return plugin.getResourceAsStream(filename);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean isOnline(String name) {
        return isOnline(name, false);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return isOnline(uuid, false);
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
    public MatrixPlayer getPlayer(UUID uuid) {
        return new BungeeMatrixPlayer(uuid);
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
    public boolean isOnline(String name, boolean here) {
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
    public Logger getLogger() {
        return plugin.getLogger();
    }
}
