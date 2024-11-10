package net.nifheim.matrix.velocity.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.velocity.listener.tasks.DisconnectTask;
import net.nifheim.matrix.velocity.listener.tasks.LoginTask;
import net.nifheim.matrix.velocity.listener.tasks.PostLoginTask;
import net.nifheim.matrix.velocity.listener.tasks.PreLoginTask;
import net.nifheim.matrix.velocity.util.LoginState;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class LoginListener {

    private final Logger logger;
    private final SchedulerAdapter scheduler;
    private final MatrixDatabaseImpl database;
    private final PlayerManagerImpl<Player> playerManager;
    private final ConcurrentMap<String, LoginState> loginStateMap = new ConcurrentHashMap<>();
    private final Set<String> profile = new HashSet<>();

    public LoginListener(Logger logger, SchedulerAdapter scheduler, MatrixDatabaseImpl database, PlayerManagerImpl<Player> playerManager) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.database = database;
        this.playerManager = playerManager;
    }


    @Subscribe(order = PostOrder.FIRST)
    public EventTask onHandshake(ConnectionHandshakeEvent event) {
        return EventTask.resumeWhenComplete(scheduler.makeFuture(() -> {
            InetAddress address = event.getConnection().getRemoteAddress().getAddress();
            ProtocolVersion version = event.getConnection().getProtocolVersion();
            Optional<InetSocketAddress> host = event.getConnection().getVirtualHost();
            String hostname = host.map(InetSocketAddress::getHostString).orElse(null);
            database.storeHandshakeRequest(address, version.getProtocol(), version.toString(), hostname);
        }));
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPreLogin(PreLoginEvent event) {
        PreLoginTask preLoginTask = new PreLoginTask(logger, event, loginStateMap.get(event.getUsername()), playerManager);
        return EventTask.resumeWhenComplete(
                scheduler.makeFuture(preLoginTask).thenRun(() -> {
                    loginStateMap.put(event.getUsername(), LoginState.PRE_LOGIN);
                    if (preLoginTask.getProfile() != null) {
                        profile.add(event.getUsername());
                    }
                    logger.info("State for {}: {}", event.getUsername(), loginStateMap.get(event.getUsername()));
                })
        );
    }

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        return EventTask.resumeWhenComplete(
                scheduler.makeFuture(new LoginTask(logger, event, playerManager)).thenRun(() -> {
                    loginStateMap.put(event.getPlayer().getUsername(), LoginState.LOGIN);
                    logger.info("State for {}: {}", event.getPlayer().getUsername(), loginStateMap.get(event.getPlayer().getUsername()));
                })
        );
    }

    @Subscribe
    public EventTask onPostLogin(PostLoginEvent event) {
        boolean premiumProfile = profile.remove(event.getPlayer().getUsername());
        return EventTask.resumeWhenComplete(
                scheduler.makeFuture(new PostLoginTask(logger, event, playerManager, premiumProfile)).thenRun(() -> {
                    loginStateMap.put(event.getPlayer().getUsername(), LoginState.POST_LOGIN);
                    logger.info("State for {}: {}", event.getPlayer().getUsername(), loginStateMap.get(event.getPlayer().getUsername()));
                })
        );
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent event) {
        return EventTask.resumeWhenComplete(
                scheduler.makeFuture(new DisconnectTask(logger, event, playerManager)).thenRun(() -> {
                    loginStateMap.remove(event.getPlayer().getUsername());
                    logger.info("Disconnect state for {}", event.getPlayer().getUsername());
                })
        );
    }
}
