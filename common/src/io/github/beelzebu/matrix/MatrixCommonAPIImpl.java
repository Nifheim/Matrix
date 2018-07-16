package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.utils.FileManager;
import java.io.File;
import java.sql.SQLException;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
public class MatrixCommonAPIImpl extends MatrixAPI {

    @Getter
    private MatrixPlugin plugin;
    private ServerInfo serverInfo;

    /**
     * Setup this api instance with the specified MethodInterface
     *
     * @param matrixPlugin instance to setup this api.
     */
    public void setup(MatrixPlugin matrixPlugin) {
        plugin = matrixPlugin;
        FileManager fileManager = new FileManager(this);
        fileManager.generateFiles();
        fileManager.updateMessages();
        serverInfo = new ServerInfo(getConfig().getString("Server Table").replaceAll(" ", ""));
        serverInfo.setServerType(ServerType.valueOf(plugin.getConfig().getString("Server Type").toUpperCase()));
        motd();
    }

    /**
     * Shutdown this api instance.
     */
    public void shutdown() {
        motd();
    }


    /**
     * The folder where the plugin data is stored.
     *
     * @return The File representing the folder.
     */
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    private void motd() {
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(""));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(rep("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(rep("        &4Matrix &fBy: &7Beelzebu")));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(rep("")));
        StringBuilder version = new StringBuilder();
        int spaces = (48 - ("v: " + plugin.getVersion()).length()) / 2;
        for (int i = 0; i < spaces; i++) {
            version.append(" ");
        }
        version.append(rep("&4v: &f" + plugin.getVersion()));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(rep(version.toString())));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(rep("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
    }

    public void log(Object msg) {
        plugin.log(rep(msg.toString()));
    }

    public void log(SQLException ex) {
        plugin.log("SQLException: ");
        plugin.log("   Database state: " + ex.getSQLState());
        plugin.log("   Error code: " + ex.getErrorCode());
        plugin.log("   Error message: " + ex.getMessage());
    }

    public void debug(Object msg) {
        plugin.log("§cDebug: §7" + msg);
    }

    public String getName(UUID uniqueId) {
        return getPlayer(uniqueId).getName();
    }

    public UUID getUUID(String name) {
        return getPlayer(name).getUniqueId();
    }

    @Override
    public RedisMessaging getRedis() {
        return null;
    }

    @Override
    public CacheProvider getCache() {
        return null;
    }

    @Override
    public MatrixDatabase getDatabase() {
        return null;
    }

    @Override
    public void log(String message) {

    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void debug(Exception ex) {

    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

}
