package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.nifheim.bukkit.util.command.MatrixCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class MatrixServersCommand extends MatrixCommand {

    public MatrixServersCommand() {
        super("mservers", "matrix.command.servers");
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] args) {
        api.getServerManager().getAllServers().thenAcceptAsync(groupServers -> {
            List<BaseComponent[]> components = new ArrayList<>();
            components.add(TextComponent.fromLegacyText(StringUtils.replace("&6Jugadores en linea: &a" + api.getPlayerManager().getOnlinePlayerCount().join())));
            for (Map.Entry<String, Set<ServerInfo>> entry : groupServers.entrySet()) {
                String groupName = entry.getKey();
                if (groupName.equals(ServerInfoImpl.PROXY_GROUP)) {
                    continue;
                }
                Set<ServerInfo> serverInfos = entry.getValue();
                components.add(TextComponent.fromLegacyText(StringUtils.replace("&7Grupo: &6" + groupName + " &7(&a" + api.getPlayerManager().getOnlinePlayerCountInGroup(groupName).join() + "&7)")));
                for (ServerInfo serverInfo : serverInfos) {
                    int playerCount = api.getPlayerManager().getOnlinePlayerCountInServer(serverInfo.getServerName()).join();
                    if (playerCount == 0 && (args.length != 1 || !args[0].equalsIgnoreCase("all"))) {
                        continue;
                    }
                    ComponentBuilder componentBuilder = new ComponentBuilder()
                            .appendLegacy(StringUtils.replace("  &f- &e")).appendLegacy(serverInfo.getServerName())
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent.fromLegacyText(StringUtils.replace("&7Click para ir a &6" + serverInfo.getServerName()))))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/btp " + serverInfo.getServerName()))
                            .appendLegacy(StringUtils.replace(" &8(&a" + playerCount + "&8)"));
                    components.add(componentBuilder.create());
                }
            }
            for (BaseComponent[] baseComponents : components) {
                commandSender.sendMessage(baseComponents);
            }
        });
    }
}
