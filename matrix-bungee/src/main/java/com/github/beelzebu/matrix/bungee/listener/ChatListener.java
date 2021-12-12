package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    private final @NotNull MatrixBungeeAPI api;
    private final String[] blockedCommands = {"version", "icanhasbukkit", "ver", "about", "luckperms", "lp", "perm", "perms", "permission", "permissions", "lpb", "pp", "pex", "powerfulperms", "permissionsex", "bungee", "plugins", "pl", "plugman"};
    private final Set<String> loggedCommands = new HashSet<>();

    public ChatListener(@NotNull MatrixBungeeAPI api) {
        this.api = api;
        for (String command : api.getConfig().getStringList("logged-commands")) {
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            loggedCommands.add(command.split(" ", 2)[0]);
        }
    }

    @EventHandler(priority = 127)
    public void onBlockedCommand(@NotNull ChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.isCommand()) {
            return;
        }
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        api.getPlayerManager().getPlayer((ProxiedPlayer) e.getSender()).thenAcceptAsync(matrixPlayer -> {
            String command = e.getMessage().toLowerCase().replaceFirst("/", "").split(" ", 2)[0];
            if (command.split(":").length > 1) {
                e.setMessage(e.getMessage().replaceFirst(command.split(":")[0] + ":", ""));
                command = command.split(":")[1];
            }
            for (String loggedCommand : loggedCommands) {
                if (command.equals(loggedCommand)) {
                    api.getDatabase().insertCommandLogEntryById(matrixPlayer.getId(), matrixPlayer.getLastServerName().join(), e.getMessage());
                }
            }
            if (((ProxiedPlayer) e.getSender()).hasPermission("matrix.admin")) {
                return;
            }
            for (String blockedCommand : blockedCommands) {
                if (blockedCommand.equals(command)) {
                    e.setMessage("/"); // send unknown command message
                }
            }
        });
    }

    @EventHandler(priority = 127)
    public void onBlockedCommand(@NotNull TabCompleteEvent e) {
        if (e.isCancelled()) {
            return;
        }
        api.getPlayerManager().getPlayer((ProxiedPlayer) e.getSender()).thenAccept(matrixPlayer -> {
            if (((ProxiedPlayer) e.getSender()).hasPermission("matrix.admin")) {
                return;
            }
            String command = e.getCursor().replaceFirst("/", "").split(":", 2)[0].toLowerCase();
            for (String blockedCommand : blockedCommands) {
                if (blockedCommand.equals(command)) {
                    e.getSuggestions().clear(); // send unknown command message
                    return;
                }
            }
            Iterator<String> it = e.getSuggestions().iterator();
            while (it.hasNext()) {
                String suggestion = it.next().replaceFirst("/", "").split(":", 2)[0].toLowerCase();
                if (suggestion.split(":").length > 1) {
                    it.remove();
                    continue;
                }
                for (String blockedCommand : blockedCommands) {
                    if (suggestion.equals(blockedCommand)) {
                        it.remove();
                        break;
                    }
                }
            }
        });
    }

    @EventHandler(priority = 127)
    public void onBlockedCommand(@NotNull TabCompleteResponseEvent e) {
        if (e.isCancelled()) {
            return;
        }
        api.getPlayerManager().getPlayerById(api.getPlayerManager().getMetaInjector().getId((ProxiedPlayer) e.getReceiver())).thenAccept(matrixPlayer -> {
            if (((ProxiedPlayer) e.getReceiver()).hasPermission("matrix.admin")) {
                return;
            }
            Iterator<String> it = e.getSuggestions().iterator();
            while (it.hasNext()) {
                String suggestion = it.next();
                String command = suggestion.toLowerCase().replaceFirst("/", "").split(" ", 2)[0];
                if (command.split(":").length > 1) {
                    it.remove();
                }
            }
        });
    }
}
