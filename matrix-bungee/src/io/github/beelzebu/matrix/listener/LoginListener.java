package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.tasks.DisconnectTask;
import io.github.beelzebu.matrix.tasks.LoginTask;
import io.github.beelzebu.matrix.tasks.PostLoginTask;
import io.github.beelzebu.matrix.tasks.PreLoginTask;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener implements Listener {

    private final MatrixAPI api = Matrix.getAPI();
    private final MatrixBungee plugin;
    private static final Map<String, Boolean> blacklist = new HashMap<>();
    private static final Map<String, Object> activeBlacklist = new HashMap<>();

    public LoginListener(MatrixBungee matrixBungee) {
        plugin = matrixBungee;
        activeBlacklist.put("http://www,stopforumspam,com/api?ip=", "yes");
        activeBlacklist.put("http://www,shroomery,org/ythan/proxycheck,php?ip=", "Y");
        try (Scanner blackList = new Scanner(new URL("http://myip.ms/files/blacklist/csf/latest_blacklist.txt").openStream())) {
            api.log("[AJB] Downloading Blacklist...");
            while (blackList.hasNextLine()) {
                String IP = blackList.nextLine();
                if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") && !blacklist.containsKey(IP)) {
                    blacklist.put(IP, true);
                }
            }
            api.log("[AJB] Blacklist successfully Downloaded");
        } catch (IOException e) {
            api.log("[AJB] Error Downloading the Blacklist");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent e) {
        if (e.getPlayer().isConnected()) {
            e.getPlayer().setTabHeader(MatrixBungee.TAB_HEADER, MatrixBungee.TAB_FOOTER);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProxyPing(ProxyPingEvent e) {
        if (plugin.isMaintenance()) {
            e.getResponse().getVersion().setProtocol(-1);
            e.getResponse().getVersion().setName("§cEn mantenimiento");
        }
        // banear los proxy en el momento en que hacen ping en la lista de servidores
        e.registerIntent(plugin);
        api.getPlugin().runAsync(() -> {
            if (isProxy(e.getConnection().getAddress().getAddress().getHostAddress())) {
                api.getPlugin().ban(e.getConnection().getAddress().getAddress().getHostAddress());
            }
            e.completeIntent(plugin);
        });
    }

    @EventHandler(priority = 127)
    public void onPreLogin(PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(plugin);
        api.getPlugin().runAsync(new PreLoginTask(plugin, e, api.getPlayer(e.getConnection().getUniqueId())));
    }

    @EventHandler(priority = -128)
    public void onLogin(LoginEvent e) {
        e.registerIntent(plugin);
        api.getPlugin().runAsync(new LoginTask(plugin, e, api.getPlayer(e.getConnection().getUniqueId())));
    }

    @EventHandler(priority = -128)
    public void onLogin(PostLoginEvent e) {
        api.getPlugin().runAsync(new PostLoginTask(e, api.getPlayer(e.getPlayer().getUniqueId())));
    }

    @EventHandler(priority = -128)
    public void onConnect(ServerConnectEvent e) {
        if (e.getReason() == ServerConnectEvent.Reason.JOIN_PROXY && e.getPlayer().getPendingConnection().isOnlineMode()) {
            e.setTarget(ProxyServer.getInstance().getServerInfo("lobby#1"));
        }
    }

    @EventHandler(priority = 127)
    public void onDisconnect(PlayerDisconnectEvent e) {
        MatrixPlayer player = api.getPlayer(e.getPlayer().getUniqueId());
        api.getPlugin().runAsync(new DisconnectTask(e, player));
    }

    public static boolean isProxy(String IP) {
        if ((IP.equals("127.0.0.1")) || (IP.equals("localhost")) || (IP.matches("192\\.168\\.[01]{1}\\.[0-9]{1,3}"))) { // está enviando información falsa
            return true;
        }
        for (String s : activeBlacklist.keySet()) {
            try (Scanner scanner = new Scanner(new URL(s.replace(",", ".") + IP).openStream())) {
                StringBuilder res = new StringBuilder();
                while (scanner.hasNextLine()) {
                    res.append(scanner.nextLine());
                }
                String[] args = ((String) activeBlacklist.get(s)).split(",");
                for (String arg : args) {
                    if (res.toString().matches(arg)) {
                        return true;
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return blacklist.containsKey(IP) && blacklist.get(IP);
    }
}
