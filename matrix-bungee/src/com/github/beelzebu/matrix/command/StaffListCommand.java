package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
        Set<ProxiedPlayer> counted = new HashSet<>();
        Matrix.getAPI().getConfig().getKeys("Staff list.Groups").forEach(k -> {
            List<ProxiedPlayer> group = ProxyServer.getInstance().getPlayers().stream().filter(pp -> !counted.contains(pp) && pp.hasPermission(Matrix.getAPI().getConfig().getString("Staff list.Groups." + k + ".Permission"))).collect(Collectors.toList());
            List<BaseComponent> tc = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(StringUtils.replace(Matrix.getAPI().getConfig().getString("Staff list.Groups." + k + ".Display")))));
            group.forEach(pp -> tc.addAll(Arrays.asList(new ComponentBuilder(pp.getName()).color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").append(TextComponent.fromLegacyText(StringUtils.replace(Matrix.getAPI().getConfig().getString("Messages.StaffList.Server")))).reset().append(pp.getServer().getInfo().getName()).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "btp " + pp.getServer().getInfo().getName()))
                    .reset().append(group.indexOf(pp) == group.size() - 1 ? "" : ", ").create())));
            counted.addAll(group);
            if (!group.isEmpty()) {
                sender.sendMessage(tc.toArray(new BaseComponent[0]));
            }
        });
    }

}
