package net.nifheim.matrix.velocity.listener.tasks;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.InboundConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.database.sql.SQLQuery;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.velocity.util.LoginState;
import org.slf4j.Logger;

public class ConnectionHandler {

    private final Logger logger;
    private final MatrixDatabaseImpl database;
    private final SchedulerAdapter scheduler;
    private final PlayerManager playerManager;
    private LoginState loginState;
    private boolean profile;
    private CompletableFuture<Long> handshakeId;

    public ConnectionHandler(Logger logger, MatrixDatabaseImpl database, SchedulerAdapter scheduler, PlayerManager playerManager) {
        this.logger = logger;
        this.database = database;
        this.scheduler = scheduler;
        this.playerManager = playerManager;
    }

    public CompletableFuture<Void> handshake(InboundConnection connection) {
        if (loginState == null) {
            loginState = LoginState.HANDSHAKE;
        }
        return scheduler.makeFuture(() -> {
            InetAddress address = connection.getRemoteAddress().getAddress();
            ProtocolVersion version = connection.getProtocolVersion();
            Optional<InetSocketAddress> host = connection.getVirtualHost();
            String hostname = host.map(InetSocketAddress::getHostString).orElse(null);
            handshakeId = scheduler.makeFuture(() -> database.saveHandshakeRequest(address, version.getProtocol(), version.toString(), hostname));
            logger.info("Handshake request saved for {} with protocol version {} and hostname {}", address, version.getProtocol(), hostname);
        });
    }

    public CompletableFuture<Void> preLogin(PreLoginEvent event) throws IllegalStateException {
        long handshake = handshakeId.join();
        if (handshake == 0) {
            throw new IllegalStateException("Handshake ID not set");
        }
        PreLoginTask preLoginTask = new PreLoginTask(logger, event, loginState, playerManager);
        return scheduler.makeFuture(preLoginTask).thenRunAsync(() -> {
            if (loginState.getPreviousState() == null) {
                loginState = LoginState.PRE_LOGIN;
            }
            logger.info("State for {}: {}", event.getUsername(), loginState);
            profile = preLoginTask.getProfile() != null;
            database.saveLoginState(handshake, SQLQuery.LoginState.PRE_LOGIN);
            MatrixPlayer player = preLoginTask.getPlayer();
            if (player != null) {
                database.save(player);
            }
        });
    }

    public CompletableFuture<Void> login(LoginEvent event) throws IllegalStateException {
        long handshake = handshakeId.join();
        if (handshake == 0) {
            throw new IllegalStateException("Handshake ID not set");
        }
        return scheduler.makeFuture(new LoginTask(logger, event, playerManager)).thenRunAsync(() -> {
            if (loginState.getPreviousState() == LoginState.PRE_LOGIN) {
                loginState = LoginState.LOGIN;
            }
            logger.info("State for {}: {}", event.getPlayer().getUsername(), loginState);
            database.saveLoginState(handshake, SQLQuery.LoginState.LOGIN);
            MatrixPlayer player = playerManager.getPlayerSync(event.getPlayer().getUniqueId());
            if (player == null) {
                return;
            }
            database.linkHandshake(player, handshake);
        });
    }

    public CompletableFuture<Void> postLogin(PostLoginEvent event) throws IllegalStateException {
        long handshake = handshakeId.join();
        if (handshake == 0) {
            throw new IllegalStateException("Handshake ID not set");
        }
        return scheduler.makeFuture(new PostLoginTask(logger, event, playerManager, profile)).thenRunAsync(() -> {
            if (loginState.getPreviousState() == LoginState.LOGIN) {
                loginState = LoginState.POST_LOGIN;
            }
            logger.info("State for {}: {}", event.getPlayer().getUsername(), loginState);
            database.saveLoginState(handshake, SQLQuery.LoginState.POST_LOGIN);
        });
    }

    public CompletableFuture<Void> disconnect(DisconnectEvent event) {
        return scheduler.makeFuture(new DisconnectTask(logger, event, playerManager));
    }
}
