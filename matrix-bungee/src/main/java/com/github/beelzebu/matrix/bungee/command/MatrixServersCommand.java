package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class MatrixServersCommand extends Command {

    public MatrixServersCommand() {
        super("mservers", "matrix.command.servers");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        Map<String, Set<ServerInfo>> servers = Matrix.getAPI().getCache().getAllServers();
        List<BaseComponent[]> components = new ArrayList<>();
        Map<String, Map<String, Set<UUID>>> groupPlayers = new HashMap<>();
        for (Map.Entry<String, Set<ServerInfo>> ent : servers.entrySet()) {
            for (ServerInfo server : ent.getValue()) {
                groupPlayers.getOrDefault(server.getGroupName(), new HashMap<>()).put(server.getServerName(), Matrix.getAPI().getCache().getOnlinePlayersInServer(server.getServerName()));
            }
        }
        int total = groupPlayers.values().stream().map(Map::values).mapToInt(Collection::size).sum();
        components.add(TextComponent.fromLegacyText(StringUtils.replace("&6Jugadores en linea: &a" + total)));
        for (Map.Entry<String, Map<String, Set<UUID>>> group : groupPlayers.entrySet()) {
            String groupName = group.getKey();
            if (Objects.equals(groupName, ServerInfoImpl.PROXY_GROUP)) {
                continue;
            }
            components.add(TextComponent.fromLegacyText(StringUtils.replace("&7Grupo: &6" + groupName + " &7(&a" + group.getValue().values().stream().mapToInt(Set::size).sum() + "&7)")));
            Map<String, Set<UUID>> playerServer = group.getValue();
            for (Map.Entry<String, Set<UUID>> server : playerServer.entrySet()) {
                String serverName = server.getKey();
                Set<UUID> players = server.getValue();
                if (players.isEmpty() && (args.length != 1 || !args[0].equalsIgnoreCase("all"))) {
                    continue;
                }
                ComponentBuilder componentBuilder = new ComponentBuilder()
                        .appendLegacy(StringUtils.replace("  &f- &e")).appendLegacy(serverName)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(StringUtils.replace("&7Click para ir a &6" + serverName))))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/btp " + serverName))
                        .appendLegacy(StringUtils.replace(" &8(&a" + players.size() + "&8)"));
                components.add(componentBuilder.create());
            }
        }
        for (BaseComponent[] baseComponents : components) {
            commandSender.sendMessage(baseComponents);
        }
    }
}
