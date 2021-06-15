package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixServersCommand extends MatrixCommand {

    public MatrixServersCommand() {
        super("mservers", "matrix.command.servers");
    }

    @Override
    public void onCommand(@NotNull CommandSender commandSender, String @NotNull [] args) {
        Matrix.getAPI().getServerManager().getAllServers().thenAcceptAsync(groupServers -> {
            List<BaseComponent[]> components = new ArrayList<>();
            components.add(TextComponent.fromLegacyText(StringUtils.replace("&6Jugadores en linea: &a" + Matrix.getAPI().getPlayerManager().getOnlinePlayerCount().join())));
            for (Map.Entry<String, Set<ServerInfo>> entry : groupServers.entrySet()) {
                String groupName = entry.getKey();
                if (groupName.equals(ServerInfoImpl.PROXY_GROUP)) {
                    continue;
                }
                Set<ServerInfo> serverInfos = entry.getValue();
                components.add(TextComponent.fromLegacyText(StringUtils.replace("&7Grupo: &6" + groupName + " &7(&a" + Matrix.getAPI().getPlayerManager().getOnlinePlayerCountInGroup(groupName).join() + "&7)")));
                for (ServerInfo serverInfo : serverInfos) {
                    int playerCount = Matrix.getAPI().getPlayerManager().getOnlinePlayerCountInServer(serverInfo.getServerName()).join();
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
