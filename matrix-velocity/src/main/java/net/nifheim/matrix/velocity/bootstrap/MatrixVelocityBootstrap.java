package net.nifheim.matrix.velocity.bootstrap;

import com.github.games647.craftapi.resolver.MojangResolver;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.nifheim.matrix.api.environment.Environment;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.RabbitMQService;
import net.nifheim.matrix.common.messaging.rabbitmq.ServerRegisterConsumer;
import net.nifheim.matrix.common.messaging.rabbitmq.ServerRequestProducer;
import net.nifheim.matrix.common.messaging.rabbitmq.ServerUnregisterConsumer;
import net.nifheim.matrix.common.player.PlayerProxy;
import net.nifheim.matrix.common.plugin.MatrixBootstrap;
import net.nifheim.matrix.common.plugin.MatrixPluginCommon;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.common.server.ServerPlatformInfo;
import net.nifheim.matrix.velocity.config.MatrixVelocityConfiguration;
import net.nifheim.matrix.velocity.listener.LoginListener;
import net.nifheim.matrix.velocity.listener.PingListener;
import net.nifheim.matrix.velocity.listener.messaging.ServerRegisterListener;
import net.nifheim.matrix.velocity.listener.messaging.ServerUnregisterListener;
import net.nifheim.matrix.velocity.scheduler.VelocitySchedulerAdapter;
import net.nifheim.matrix.velocity.task.ServerCleanupTask;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MatrixVelocityBootstrap implements MatrixBootstrap<Player> {

    public static final MojangResolver RESOLVER = new MojangResolver();
    private final ProxyServer server;
    private final Logger logger;
    private final PluginDescription description;
    private SchedulerAdapter schedulerAdapter;

    private final MatrixVelocityConfiguration configuration;
    private final MatrixPluginCommon<Player> plugin;
    private final ServerPlatformInfo serverPlatformInfo;

    @Inject
    public MatrixVelocityBootstrap(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger, PluginDescription description) {
        this.server = server;
        this.logger = logger;
        this.description = description;
        if (!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdirs();
        }
        File configFile = new File(dataDirectory.toFile(), "config.yml");
        if (!configFile.exists()) {
            InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.yml");
            if (configStream == null) {
                throw new RuntimeException("Default config not found");
            }
            try {
                Files.copy(configStream, configFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.serverPlatformInfo = new ServerPlatformInfo(server.getBoundAddress().getAddress().getHostAddress(), server.getBoundAddress().getPort(), server.getConfiguration().getShowMaxPlayers());
        this.configuration = new MatrixVelocityConfiguration(new File(dataDirectory.toFile(), "config.yml"));
        plugin = new MatrixPluginCommon<>(this, configuration, new PlayerProxy<>() {
            @Override
            public @Nullable Player getPlatformPlayer(UUID uniqueId) {
                return server.getPlayer(uniqueId).orElse(null);
            }
        });
        plugin.load();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        schedulerAdapter = new VelocitySchedulerAdapter(this);
        try {
            plugin.enable();
        } catch (Exception e) {
            getPlatformLogger().error("Failed to initialize Matrix plugin", e);
        }
        // register this server on the server manager
        schedulerAdapter.asyncRepeating(new ServerCleanupTask(logger, plugin.getServerManager(), server), 1, TimeUnit.MINUTES);
        plugin.getServerManager().addServer(plugin.getServerInfo());
        server.getEventManager().register(this, new LoginListener(logger, schedulerAdapter, plugin.getDatabase(), plugin.getPlayerManager()));
        server.getEventManager().register(this, new PingListener());

        MessagingService messagingService = (MessagingService) plugin.getApi().getMessaging();
        RabbitMQService rabbitMQService = plugin.getApi().getService(RabbitMQService.class);

        ServerRegisterConsumer registerConsumer = new ServerRegisterConsumer(rabbitMQService, logger);
        ServerUnregisterConsumer unregisterConsumer = new ServerUnregisterConsumer(rabbitMQService, logger);

        messagingService.registerConsumer(ServerRegisterConsumer.class, registerConsumer);
        messagingService.registerConsumer(ServerUnregisterConsumer.class, unregisterConsumer);

        try {
            registerConsumer.consume(new ServerRegisterListener(logger, server, plugin.getServerManager()));
            unregisterConsumer.consume(new ServerUnregisterListener(logger, server, plugin.getServerManager()));
        } catch (IOException e) {
            logger.error("Error starting messaging listeners", e);
        }

        try {
            messagingService.getProducer(ServerRequestProducer.class).sendMessage(new ServerRequestMessage());
        } catch (IOException | java.util.concurrent.TimeoutException e) {
            logger.error("Error sending server request message", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (plugin != null) {
            plugin.getServerManager().removeServer(plugin.getServerInfo());
            plugin.disable();
        }
    }

    @Override
    public @NotNull SchedulerAdapter getScheduler() {
        return schedulerAdapter;
    }

    @Override
    public Environment getEnvironment() {
        return Environment.EnvironmentImpl.PROXY;
    }

    @Override
    public Audience getConsole() {
        return server.getConsoleCommandSource();
    }

    @Override
    public String getVersion() {
        return description.getVersion().orElse("unknown");
    }

    @Override
    public MatrixPluginCommon<Player> getPlugin() {
        return plugin;
    }

    @Override
    public ServerPlatformInfo getServerPlatformInfo() {
        return serverPlatformInfo;
    }

    @Override
    public @NotNull Logger getPlatformLogger() {
        return logger;
    }

    public @NotNull ProxyServer getServer() {
        return server;
    }

    public @NotNull MatrixVelocityConfiguration getConfiguration() {
        return configuration;
    }
}
