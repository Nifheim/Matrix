package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener implements Listener {

    private final Main plugin;
    private final MatrixAPI core;
    private final Map<String, Boolean> blacklist = new HashMap<>();
    private final Map<String, Object> activeBlacklist = new HashMap<>();

    public LoginListener(Main main) {
        plugin = main;
        core = MatrixAPI.getInstance();

        activeBlacklist.put("http://www,stopforumspam,com/api?ip=", "yes");
        activeBlacklist.put("http://www,shroomery,org/ythan/proxycheck,php?ip=", "Y");
        try {
            Scanner Blacklist = new Scanner(new URL("http://myip.ms/files/blacklist/csf/latest_blacklist.txt").openStream());
            System.out.println("[AJB] Downloading Blacklist...");
            while (Blacklist.hasNextLine()) {
                String IP = Blacklist.nextLine();
                if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") && !blacklist.containsKey(IP)) {
                    blacklist.put(IP, true);
                }
            }
            Blacklist.close();
            System.out.println("[AJB] Blacklist successfully Downloaded");
        } catch (IOException e) {
            System.out.println("[AJB] Error Downloading the Blacklist");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent e) {
        if (e.getPlayer().isConnected()) {
            if (e.getPlayer().getServer().getInfo().getName().startsWith("towny")) {
                e.getPlayer().setTabHeader(Main.TAB_HEADER, Main.TAB_FOOTER);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProxyPing(ProxyPingEvent e) {
        if (plugin.isMaintenance()) {
            e.getResponse().getVersion().setProtocol(666);
            e.getResponse().getVersion().setName("§4§lEn Mantenimiento");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(LoginEvent e) {
        core.getMethods().runAsync(() -> {
            e.registerIntent(plugin);
            if (core.getUUID(e.getConnection().getName(), true) != null && !core.getUUID(e.getConnection().getName(), true).equals(e.getConnection().getUniqueId())) {
                e.setCancelReason(TextComponent.fromLegacyText("Tu UUID no coincide con la UUID que hay en nuestra base de datos"));
                e.setCancelled(true);
            }
            e.completeIntent(plugin);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(LoginEvent e) {
        if (plugin.isMaintenance() && !core.getConfig().getStringList("Whitelist").contains(e.getConnection().getName())) {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText("§c§lEn Mantenimiento\n7\n§7Lo sentimos, pero en este momento estamos en mantenimiento"));
        }
    }

    public Boolean isProxy(String IP) {
        if ((IP.equals("127.0.0.1")) || (IP.equals("localhost")) || (IP.matches("192\\.168\\.[01]{1}\\.[0-9]{1,3}"))) {
            return true;
        }
        for (String s : activeBlacklist.keySet()) {
            try {
                String res = "";
                Scanner scanner = new Scanner(new URL(s.replace(",", ".") + IP).openStream());
                while (scanner.hasNextLine()) {
                    res = res + scanner.nextLine();
                }
                String[] args = ((String) activeBlacklist.get(s)).split(",");
                for (String arg : args) {
                    if (res.matches(arg)) {
                        return true;
                    }

                    scanner.close();
                }
            } catch (Exception e) {
            }
        }
        return blacklist.containsKey(IP) && blacklist.get(IP);
    }
}
