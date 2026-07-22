package net.nifheim.matrix.common.api;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nifheim.matrix.api.MatrixAPI;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.database.MatrixDatabase;
import net.nifheim.matrix.api.environment.Environment;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.api.service.MatrixService;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.messaging.rabbitmq.RabbitMQService;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.plugin.MatrixPluginCommon;
import net.nifheim.matrix.common.task.HeartbeatTask;
import net.nifheim.matrix.common.util.MaintenanceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class MatrixCommon <P extends Identified> implements MatrixAPI {

    public static final String DOMAIN_NAME = "mc.nifheim.net";
    public static final Set<String> DOMAIN_NAMES = Set.of(DOMAIN_NAME);
    public static Gson GSON;
    private final @NotNull MatrixPluginCommon<P> plugin;
    private static final Map<Class<? extends MatrixService>, MatrixService> SERVICE_REGISTRY = new HashMap<>();

    public MatrixCommon(@NotNull MatrixPluginCommon<P> plugin) {
        this.plugin = plugin;
    }

    public @NotNull MaintenanceManager getMaintenanceManager() {
        return plugin.getMaintenanceManager();
    }

    public @NotNull RabbitMQService getRabbitMQManager() {
        return plugin.getRabbitMQManager();
    }

    @Override
    public Logger getLogger() {
        return plugin.getBootstrap().getPlatformLogger();
    }

    @Override
    public @NotNull MessagingService getMessaging() {
        return getService(MessagingService.class);
    }

    @Override
    public @NotNull MatrixDatabaseImpl getDatabase() {
        MatrixDatabase database = getService(MatrixDatabase.class);
        if (database instanceof MatrixDatabaseImpl) {
            return (MatrixDatabaseImpl) database;
        }
        throw new UnsupportedOperationException("Database service is not of the correct type");
    }

    @Override
    public @NotNull Environment getEnvironment() {
        return plugin.getBootstrap().getEnvironment();
    }

    public @NotNull ServerInfo getServerInfo() {
        return plugin.getServerInfo();
    }

    @Override
    public @NotNull ServerManager getServerManager() {
        return plugin.getServerManager();
    }

    @Override
    public @NotNull PlayerManagerImpl<P> getPlayerManager() {
        return plugin.getPlayerManager();
    }

    @Override
    public <S extends MatrixService> void setService(Class<S> type, MatrixService service) throws UnsupportedOperationException {
        // don't allow to change database service
        if (service instanceof MatrixDatabase && SERVICE_REGISTRY.containsKey(MatrixDatabase.class)) {
            throw new UnsupportedOperationException("Database service cannot be changed");
        }
        SERVICE_REGISTRY.put(type, service);
    }

    public <S extends MatrixService> S getService(Class<S> service) {
        MatrixService registeredService = SERVICE_REGISTRY.get(service);
        if (registeredService == null) {
            throw new UnsupportedOperationException("Service not registered");
        }
        if (!service.isInstance(registeredService)) {
            throw new UnsupportedOperationException("Service registered is not of the correct type");
        }
        return service.cast(registeredService);
    }

    public void reload() {
        plugin.getConfiguration().reload();
        getLogger().info("Reloaded config and messages.");
    }

    /**
     * Setup this api instance
     */
    public void setup() {
        motd(plugin.getConsole());
        plugin.getBootstrap().getScheduler().asyncRepeating(new HeartbeatTask(getServerManager(), getLogger()), 1, TimeUnit.MINUTES);
        MatrixProvider.setAPI(this);
    }

    public void motd(@NotNull Audience audience) {
        audience.sendMessage(Component.empty());
        audience.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
        audience.sendMessage(center("&4Matrix &fBy: &7Beelzebu"));
        audience.sendMessage(Component.empty());
        audience.sendMessage(center("&4v: &f" + plugin.getVersion()));
        audience.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
        audience.sendMessage(center("&7Server Info:"));
        audience.sendMessage(center("&7Group: &6" + getServerInfo().getGroupName() + " &7Name: &6" + getServerInfo().getName()));
        audience.sendMessage(center("&7ServerType: &6" + getServerInfo().getServerType() + " &7GameMode: &6" + getServerInfo().getDefaultGameMode() + " &7Lobby: &6" + getServerInfo().getDefaultLobbyName()));
        audience.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
    }

    /**
     * Center a message in a fixed width of 48 characters.
     *
     * @param message message to center.
     * @return a {@link Component} with the centered message.
     */
    private Component center(String message) {
        StringBuilder plainMessage = new StringBuilder();
        // strip legacy color codes from message
        char[] charArray = message.toCharArray();
        for (int i = 0; i < charArray.length - 1; ++i) {
            // if char is & and next char is a color code
            // remove & and the color code from the array
            // valid color codes: 0123456789abcdefklmnor
            if (charArray[i] != '&' || "0123456789abcdefklmnor".indexOf(Character.toLowerCase(charArray[i + 1])) == -1) {
                plainMessage.append(charArray[i]);
            }
        }
        int spaces = (54 - plainMessage.length()) / 2;
        return LegacyComponentSerializer.legacyAmpersand().deserialize(" ".repeat(Math.max(0, spaces)) + message);
    }

    /**
     * Shutdown this api instance.
     */
    public void shutdown() {
        motd(plugin.getConsole());
    }
}
