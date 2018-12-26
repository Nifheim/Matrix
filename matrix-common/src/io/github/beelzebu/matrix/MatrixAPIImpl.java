package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.cache.CacheProviderImpl;
import io.github.beelzebu.matrix.database.MongoStorage;
import io.github.beelzebu.matrix.utils.FileManager;
import java.io.File;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
@Getter
public class MatrixAPIImpl extends MatrixAPI {

    private final MatrixPlugin plugin;
    private final MongoStorage database;
    private final RedisMessaging redis;
    private final CacheProviderImpl cache;
    private final ServerInfo serverInfo;

    MatrixAPIImpl(MatrixPlugin plugin) {
        this.plugin = plugin;
        database = new MongoStorage(plugin.getConfig().getString("Database.Host"), 27017, "admin", "matrix", plugin.getConfig().getString("Database.Password"), "admin");
        redis = new RedisMessaging(this);
        cache = new CacheProviderImpl();
        serverInfo = new ServerInfo(plugin.getConfig().getString("Server Table").replaceAll(" ", ""));
        serverInfo.setServerType(ServerType.valueOf(plugin.getConfig().getString("Server Type").toUpperCase()));
    }

    /**
     * The folder where the plugin data is stored.
     *
     * @return The File representing the folder.
     */
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public String getName(UUID uniqueId) {
        return getPlayer(uniqueId).getName();
    }

    public UUID getUniqueId(String name) {
        return getPlayer(name).getUniqueId();
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return false;
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

    /**
     * Setup this api instance
     */
    void setup() {
        FileManager fileManager = new FileManager(this);
        fileManager.generateFiles();
        fileManager.updateMessages();
        motd();
    }

    /**
     * Shutdown this api instance.
     */
    void shutdown() {
        motd();
    }
}
