package net.nifheim.matrix.paper.command.staff;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.paper.command.MatrixCommand;
import net.nifheim.matrix.paper.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class BungeeTPCommand extends MatrixCommand {

    private final ServerManager serverManager;
    private final Cache<String, ServerInfo> serverInfoCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).weakValues().build();

    public BungeeTPCommand(Plugin plugin, ServerManager serverManager) {
        super(plugin, "btp", "matrix.command.btp", false, "bungeetp");
        this.serverManager = serverManager;
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> serverManager.getAllServers().thenAccept(map -> map.values().stream().flatMap(Collection::stream).forEach(serverInfo -> serverInfoCache.put(serverInfo.getName(), serverInfo))), 5, 120);
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {

        if (args.length == 1) {
            if (sender instanceof Player player) {
                player.sendMessage(Component.text("Searching server with name: " + args[0]).color(TextColor.color(0x3FFF59)));
                serverManager.getServer(args[0]).thenAccept(optionalServerInfo -> {
                    if (optionalServerInfo.isPresent()) {
                        BungeeUtil.move(player, optionalServerInfo.get().getName());
                    } else {
                        player.sendMessage(Component.text("There is no server with the specified name.").color(TextColor.color(0xFF3F46)));
                    }
                });
            } else {
                sender.sendMessage(Component.text("You must be a player to use this command.").color(TextColor.color(0xFF3F46)));
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                sender.sendMessage(Component.text("Searching server with name: " + args[1]).color(TextColor.color(0x3FFF59)));
                serverManager.getServer(args[1]).thenAccept(optionalServerInfo -> {
                    if (optionalServerInfo.isPresent()) {
                        BungeeUtil.move(target, args[0], optionalServerInfo.get().getName());
                    } else {
                        sender.sendMessage(Component.text("There is no server with the specified name.").color(TextColor.color(0xFF3F46)));
                    }
                });
            } else {
                sender.sendMessage(Component.text("There is no player with the specified name.").color(TextColor.color(0xFF3F46)));
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            List<String> results = new ArrayList<>();
            String partial = args[0];
            results.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(playerName -> playerName.toLowerCase().startsWith(partial.toLowerCase(Locale.ROOT))).toList());
            addServers(partial, results);
            Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
            return results;

        } else if (args.length == 2) {
            List<String> results = new ArrayList<>();
            String partial = args[1];
            addServers(partial, results);
            return results;
        }
        return super.tabComplete(sender, alias, args);
    }

    private void addServers(String partial, List<String> results) {
        // read all the servers from serverInfoCache and add to results
        serverInfoCache.asMap().keySet().stream()
                .filter(serverName -> serverName.toLowerCase().startsWith(partial.toLowerCase(Locale.ROOT)))
                .forEach(results::add);
    }
}
