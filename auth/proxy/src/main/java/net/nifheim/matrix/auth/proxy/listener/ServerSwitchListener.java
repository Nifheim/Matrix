package net.nifheim.matrix.auth.proxy.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Comparator;
import net.nifheim.matrix.auth.proxy.config.ConfigurationFile;
import org.slf4j.Logger;

public class ServerSwitchListener extends MatrixPlayerListener {

    private final Logger logger;
    private final ConfigurationFile configuration;
    private final ProxyServer proxyServer;

    public ServerSwitchListener(Logger logger, ConfigurationFile configuration, ProxyServer proxyServer) {
        this.logger = logger;
        this.configuration = configuration;
        this.proxyServer = proxyServer;
    }

    // set the initial server for premium players
    @Subscribe
    public void onInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        if (player.isOnlineMode()) {
            logger.info("Setting initial server for {} to {}", player.getUsername(), configuration.getString("login.target"));
            proxyServer.getServer(configuration.getString("login.target")).ifPresent(event::setInitialServer);
        } else {
            proxyServer.matchServer("auth").stream().min(Comparator.comparingInt(a -> a.getPlayersConnected().size())).ifPresent(event::setInitialServer);
        }
    }

    // deny server switch for non-logged in players
    @Subscribe
    public EventTask onServerSwitch(ServerPreConnectEvent event) {
        return wrapTask(executePlayerLogic(event.getPlayer().getUniqueId(), matrixPlayer -> {
            if (event.getPreviousServer() == null) {
                return;
            }
            if (matrixPlayer == null || !matrixPlayer.isLoggedIn()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
        }));
    }
}
