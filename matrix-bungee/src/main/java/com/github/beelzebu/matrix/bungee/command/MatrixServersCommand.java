package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.ServerRegisterMessage;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
        components.add(TextComponent.fromLegacyText(StringUtils.replace("&6Jugadores en linea: &a" + ProxyServer.getInstance().getPlayers().size())));
        for (Map.Entry<String, Set<ServerInfo>> ent : servers.entrySet()) {
            int groupCount = 0;
            for (ServerInfo server : ent.getValue()) {
                int playerCount = (int) ProxyServer.getInstance().getPlayers().stream().filter(proxiedPlayer -> proxiedPlayer.getServer().getInfo().getName().equals(server.getServerName())).count();
                groupCount += playerCount;
            }
            components.add(TextComponent.fromLegacyText(StringUtils.replace("&7Grupo: &6" + ent.getKey() + " &7(&a" + groupCount + "&7)")));
            for (ServerInfo server : ent.getValue()) {
                int playerCount = (int) ProxyServer.getInstance().getPlayers().stream().filter(proxiedPlayer -> proxiedPlayer.getServer().getInfo().getName().equals(server.getServerName())).count();
                if (playerCount == 0 && (args.length != 1 || !args[0].equalsIgnoreCase("all"))) {
                    continue;
                }
                ComponentBuilder componentBuilder = new ComponentBuilder()
                        .appendLegacy(StringUtils.replace("  &f- &e")).appendLegacy(server.getServerName())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(StringUtils.replace("&7Click para ir a &6" + server))))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/btp " + server))
                        .appendLegacy(StringUtils.replace(" &8(&a" + playerCount + "&8)"));
                components.add(componentBuilder.create());
            }
        }
        for (BaseComponent[] baseComponents : components) {
            commandSender.sendMessage(baseComponents);
        }
    }
}
