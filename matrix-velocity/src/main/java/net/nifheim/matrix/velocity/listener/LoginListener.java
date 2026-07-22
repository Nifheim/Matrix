package net.nifheim.matrix.velocity.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.velocity.listener.tasks.ConnectionHandler;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class LoginListener {

    private final Logger logger;
    private final SchedulerAdapter scheduler;
    private final MatrixDatabaseImpl database;
    private final PlayerManagerImpl<Player> playerManager;
    private final ConcurrentMap<String, ConnectionHandler> namedConnections = new ConcurrentHashMap<>();

    public LoginListener(Logger logger, SchedulerAdapter scheduler, MatrixDatabaseImpl database, PlayerManagerImpl<Player> playerManager) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.database = database;
        this.playerManager = playerManager;
    }


    @Subscribe(order = PostOrder.FIRST)
    public EventTask onHandshake(ConnectionHandshakeEvent event) {
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        ConnectionHandler connectionHandler = namedConnections.computeIfAbsent(address, k -> new ConnectionHandler(logger, database, scheduler, playerManager));
        logger.info("Handling handshake for connection: {}", address);
        return EventTask.resumeWhenComplete(connectionHandler.handshake(event.getConnection()));
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPreLogin(PreLoginEvent event) {
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        // we should already have a connection handler since the player must have passed the connetion handshake
        logger.info("PreLogin connection: {}", address);
        ConnectionHandler connectionHandler = namedConnections.get(address);
        // if there is no connection handler, we must cancel the event
        if (connectionHandler == null) {
            logger.warn("No connection handler found for {}", address);
            return EventTask.async(() -> event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("\nConnection cancelled by Matrix\n"))));
        }
        namedConnections.put(event.getUsername(), connectionHandler);
        namedConnections.remove(address);
        connectionHandler = namedConnections.get(event.getUsername());
        logger.info("Handling pre login for connection: {}", address);
        try {
            return EventTask.resumeWhenComplete(connectionHandler.preLogin(event));
        } catch (IllegalStateException e) {
            logger.error("Connection cancelled by Matrix", e);
            return EventTask.async(() -> event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("\nConnection cancelled by Matrix\n"))));
        }
    }

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        // we should already have a connection handler since the player must have passed the connetion handshake
        ConnectionHandler connectionHandler = namedConnections.get(event.getPlayer().getUsername());
        logger.info("Handling login for connection: {}", event.getPlayer().getRemoteAddress());
        logger.info("Connection handlers: {}", namedConnections.size());
        // if there is no connection handler, we must cancel the event
        if (connectionHandler == null) {
            return EventTask.async(() -> event.setResult(LoginEvent.ComponentResult.denied(Component.text("\nConnection cancelled by Matrix\n"))));
        }
        return EventTask.resumeWhenComplete(connectionHandler.login(event));
    }

    @Subscribe
    public EventTask onPostLogin(PostLoginEvent event) {
        // we should already have a connection handler since the player must have passed the connetion handshake
        ConnectionHandler connectionHandler = namedConnections.get(event.getPlayer().getUsername());
        // if there is no connection handler, we must cancel the event
        if (connectionHandler == null) {
            return EventTask.async(() -> event.getPlayer().disconnect(Component.text("\nConnection cancelled by Matrix\n")));
        }
        return EventTask.resumeWhenComplete(connectionHandler.postLogin(event));
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent event) {
        // we should already have a connection handler since the player must have passed the connetion handshake
        ConnectionHandler connectionHandler = namedConnections.get(event.getPlayer().getUsername());
        // if there is no connection handler, we must cancel the event
        if (connectionHandler == null) {
            return EventTask.async(() -> event.getPlayer().disconnect(Component.text("\nConnection cancelled by Matrix\n")));
        }
        return EventTask.resumeWhenComplete(connectionHandler.disconnect(event).thenRun(() -> {
            namedConnections.remove(event.getPlayer().getRemoteAddress().getAddress().getHostAddress());
            namedConnections.remove(event.getPlayer().getUsername());
        }));
    }
}
