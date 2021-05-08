package com.github.beelzebu.matrix.bungee.tablist;

import com.github.beelzebu.matrix.api.Matrix;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public final class TablistManager {

    private final static String[] TAB_HEADER = {
            "",
            "&e&lINDIO PIKARO",
            "&f&lNETWORK",
            ""
    };
    private final static String[] TAB_FOOTER = {
            "&7%server%",
            "",
            " &e&m-+&8&m----------------------------&e&m+-&r ",
            "&6&lWEB: &fwww.indiopikaro.cl",
            "&6&lDISCORD: &finvite.gg/indiopikaro",
            "&6&lINSTAGRAM: &f@pikaroposting",
            " &e&m-+&8&m-----------------------&e&m+-&r ",
            "&emc.indiopikaro.cl",
            ""
    };
    private static final Map<String, String> SERVER_ALIASES = new HashMap<>();

    public static void init() {
        SERVER_ALIASES.clear();
        Matrix.getAPI().getConfig().getStringList("server alias").forEach(line -> SERVER_ALIASES.put(line.split(":::")[0], line.split(":::")[1]));
    }

    public static BaseComponent[] getTabHeader(ProxiedPlayer proxiedPlayer) {
        return getComponents(proxiedPlayer, TAB_HEADER);
    }

    public static BaseComponent[] getTabFooter(ProxiedPlayer proxiedPlayer) {
        return getComponents(proxiedPlayer, TAB_FOOTER);
    }

    private static String getServerAlias(String server) {
        server = server.toLowerCase();
        for (Map.Entry<String, String> entry : SERVER_ALIASES.entrySet()) {
            if (server.matches(entry.getKey()) || server.startsWith(entry.getKey()) || Objects.equals(server, entry.getKey())) {
                return entry.getValue();
            }
        }
        return server;
    }

    private static BaseComponent[] getComponents(ProxiedPlayer proxiedPlayer, String[] lines) {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = ChatColor.translateAlternateColorCodes('&', lines[i].replace("%server%", getServerAlias(proxiedPlayer.getServer().getInfo().getName())).replace("%player%", proxiedPlayer.getName()));
            if (i == lines.length - 1) {
                header.append(line);
            } else {
                header.append(line).append("\n");
            }
        }
        return TextComponent.fromLegacyText(header.toString());
    }
}
