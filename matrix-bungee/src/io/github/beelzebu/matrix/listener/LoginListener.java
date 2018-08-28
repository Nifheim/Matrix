package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.player.BungeeMatrixPlayer;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
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
    private final Main plugin;
    private final Map<String, Boolean> blacklist = new HashMap<>();
    private final Map<String, Object> activeBlacklist = new HashMap<>();

    public LoginListener(Main main) {
        plugin = main;
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
        if (e.getPlayer().isConnected() && e.getPlayer().getServer().getInfo().getName().equalsIgnoreCase("towny")) {
            e.getPlayer().setTabHeader(Main.TAB_HEADER, Main.TAB_FOOTER);
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

    @EventHandler(priority = -100)
    public void onPreLogin(PreLoginEvent e) {
        e.registerIntent(plugin);
        api.getPlugin().runAsync(() -> {
            if (isProxy(e.getConnection().getAddress().getAddress().getHostAddress())) {
                api.getPlugin().ban(e.getConnection().getAddress().getAddress().getHostAddress());
                e.completeIntent(plugin);
                return;
            }
            if (api.getPlayer(e.getConnection().getName()) != null && !api.getPlayer(e.getConnection().getName()).getUniqueId().equals(e.getConnection().getUniqueId())) {
                e.getConnection().setUniqueId(api.getPlayer(e.getConnection().getName()).getUniqueId());
                e.setCancelReason(TextComponent.fromLegacyText("Tu UUID no coincide con la UUID que hay en nuestra base de datos\ntus datos fueron registrados por seguridad."));
                e.setCancelled(true);
            }
            e.completeIntent(plugin);
        });
    }

    @EventHandler(priority = -99)
    public void onLogin(LoginEvent e) {
        if (plugin.isMaintenance() && !api.getConfig().getStringList("Whitelist").contains(e.getConnection().getName())) {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText("§c§lEn Mantenimiento\n7\n§7Lo sentimos, pero en este momento estamos en mantenimiento"));
        }
    }

    @EventHandler(priority = -128)
    public void onJoin(PostLoginEvent e) {
        // obtener el usuario desde la api, que revisará el cache o la base de datos, en caso de que sea nulo debemos
        // crearlo ya que es un usurio nuevo
        api.getPlugin().runAsync(() -> Optional.ofNullable(api.getPlayer(e.getPlayer().getUniqueId())).orElse(new BungeeMatrixPlayer(e.getPlayer().getUniqueId())).save());
    }

    @EventHandler(priority = -128)
    public void onConnect(ServerConnectEvent e) {
        if (e.getReason() == ServerConnectEvent.Reason.JOIN_PROXY && e.getPlayer().getPendingConnection().isOnlineMode() && false) {
            e.setTarget(ProxyServer.getInstance().getServerInfo("lobby#1"));
        }
    }

    private boolean isProxy(String IP) {
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
