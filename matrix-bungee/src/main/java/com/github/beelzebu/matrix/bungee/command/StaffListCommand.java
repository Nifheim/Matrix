package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
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
public class StaffListCommand extends Command {

    public StaffListCommand() {
        super("stafflist", "matrix.command.stafflist", "sl");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&7[&6!&7] &fLista de Staff conectados &7[&6!&7]")));
        sender.sendMessage(new TextComponent(" "));
        Matrix.getAPI().getPlayerManager().getOnlinePlayers().thenAccept(onlinePlayers -> {
            Map<Group, List<UUID>> groupPlayers = new LinkedHashMap<>();
            for (Group loadedGroup : LuckPermsProvider.get().getGroupManager().getLoadedGroups().stream().sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0))).collect(Collectors.toCollection(LinkedHashSet::new))) {
                if (loadedGroup.getName().equals("default")) {
                    continue;
                }
                if (loadedGroup.getName().startsWith("rank")) {
                    continue;
                }
                if (loadedGroup.getName().startsWith("vip")) {
                    continue;
                }
                groupPlayers.computeIfAbsent(loadedGroup, group -> {
                    List<UUID> players = new ArrayList<>();
                    Iterator<UUID> it = onlinePlayers.iterator();
                    while (it.hasNext()) {
                        UUID uniqueId = it.next();
                        User user = LuckPermsProvider.get().getUserManager().loadUser(uniqueId).join();
                        if (user.getInheritedGroups(QueryOptions.defaultContextualOptions()).contains(loadedGroup)) {
                            players.add(uniqueId);
                            it.remove();
                        }
                    }
                    return players;
                });
            }
            groupPlayers.forEach((group, players) -> {
                List<BaseComponent> tc = Arrays.asList(TextComponent.fromLegacyText((group.getDisplayName() == null ? group.getName() : group.getDisplayName()) + "&f&l>> "));
                players.stream().map(uuid -> Matrix.getAPI().getPlayerManager().getPlayer(uuid).join()).forEach(player -> tc.addAll(Arrays.asList(
                        new ComponentBuilder().color(ChatColor.YELLOW).append(player.getName())
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(StringUtils.replace("&7Click para ir a &e" + player.getLastServerName()))))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/btp " + player.getLastServerName()))
                                .append(players.indexOf(player.getUniqueId()) == players.size() - 1 ? "" : ", ").reset().create())));
                sender.sendMessage(tc.toArray(new BaseComponent[0]));
            });
        });
    }

}
