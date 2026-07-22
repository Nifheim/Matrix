package net.nifheim.matrix.auth.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.auth.proxy.config.ConfigurationFile;
import net.nifheim.matrix.auth.proxy.listener.CommandListener;
import net.nifheim.matrix.auth.proxy.listener.ServerSwitchListener;
import net.nifheim.matrix.auth.proxy.listener.messaging.LoginListener;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.messaging.rabbitmq.LoginConsumer;
import net.nifheim.matrix.common.messaging.rabbitmq.RabbitMQService;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;

public class MatrixAuth {

    private final ProxyServer server;
    private final Path dataDirectory;
    private final Logger logger;
    private final ConfigurationFile configuration;

    @Inject
    public MatrixAuth(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
        if (!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdirs();
        }
        File configFile = dataDirectory.resolve("config.yml").toFile();
        if (configFile.exists()) {
            logger.info("Configuration file already exists, skipping creation");
        } else {
            logger.info("Creating configuration file");
            InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.yml");
            if (configStream == null) {
                throw new RuntimeException("Default config not found");
            }
            try {
                Files.copy(configStream, configFile.toPath());
                logger.info("Configuration file created");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            configuration = new ConfigurationFile(configFile.toPath());
        } catch (ConfigurateException e) {
            logger.error("Failed to load configuration file");
            throw new RuntimeException(e);
        }
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerListeners();
    }

    private void registerListeners() {
        server.getEventManager().register(this, new CommandListener(logger, configuration));
        server.getEventManager().register(this, new ServerSwitchListener(logger, configuration, server));

        MessagingService messagingService = MatrixProvider.getAPI().getService(MessagingService.class);
        LoginConsumer loginConsumer = new LoginConsumer(MatrixProvider.getAPI().getService(RabbitMQService.class), logger);
        messagingService.registerConsumer(LoginConsumer.class, loginConsumer);
        try {
            loginConsumer.consume("auth:proxy:login", new LoginListener(logger, configuration, server));
        } catch (IOException e) {
            logger.error("Error starting LoginConsumer", e);
        }
    }

    public ConfigurationFile getConfiguration() {
        return configuration;
    }
}

