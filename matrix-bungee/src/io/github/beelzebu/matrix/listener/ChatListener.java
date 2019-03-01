package io.github.beelzebu.matrix.listener;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.util.SpamUtils;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    private final MatrixAPI api;
    private final String[] blockedCommands = {"version", "icanhasbukkit", "ver", "about", "lp", "lpb", "perms", "luckperms", "pp", "pex", "powerfulperms", "permissionsex", "bungee"};
    private final String[] disabledServers = {"auth", "auth#1", "auth#2"};
    private final Cache<UUID, String> messageEquals = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
    private final Cache<UUID, Boolean> messageCooldown = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private final ListMultimap<Integer, String> punishments = ArrayListMultimap.create();
    private final ListMultimap<Integer, String> spamPunishments = ArrayListMultimap.create();

    public ChatListener(MatrixAPI api) {
        this.api = api;
        api.getConfig().getStringList("Censoring.command").forEach(k -> punishments.put(Integer.parseInt(k.split(";", 2)[0]), k.split(";", 2)[1]));
        api.getConfig().getStringList("AntiSpam.Commands").forEach(k -> spamPunishments.put(Integer.parseInt(k.split(";", 2)[0]), k.split(";", 2)[1]));
        SpamUtils.WHITELIST.addAll(api.getConfig().getStringList("AntiSpam.Whitelist"));
    }

    @EventHandler(priority = 127)
    public void onChat(ChatEvent e) { // censoring
        if (e.isCancelled() || e.isCommand()) {
            return;
        }
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= 5) {
            return;
        }
        if (e.getSender() instanceof ProxiedPlayer) {
            if (((ProxiedPlayer) e.getSender()).hasPermission("matrix.helper")) {
                return;
            }
            if (Stream.of(disabledServers).anyMatch(server -> ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server))) {
                return;
            }
            String regex = checkCensoring(e.getMessage().toLowerCase());
            if (regex != null) {
                String word = e.getMessage().replaceAll(e.getMessage().replaceAll(regex, ""), "");
                broadcast((ProxiedPlayer) e.getSender(), e.getMessage(), BroadcastType.CENSORING);
                e.setMessage(e.getMessage().replaceAll(regex, Strings.repeat("*", word.length())));
                MatrixPlayer matrixPlayer = api.getPlayer(((ProxiedPlayer) e.getSender()).getUniqueId());
                if (matrixPlayer != null) {
                    matrixPlayer.incrCensoringLevel();
                    //int level = matrixPlayer.getCensoringLevel();
                    //punishments.get(punishments.containsKey(level) ? level : punishments.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()).get(0)).forEach(k -> ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), k.replace("%name%", ((ProxiedPlayer) e.getSender()).getName()).replace("%word%", e.getMessage().replaceAll(e.getMessage().replaceAll(censoring, ""), "")).replace("%count%", String.valueOf(level))));
                    api.getConfig().getStringList("Messages.Censored").forEach(line -> ((ProxiedPlayer) e.getSender()).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%word%", e.getMessage().replaceAll(e.getMessage().replaceAll(regex, ""), ""))))));
                } else {
                    throw new RuntimeException(((ProxiedPlayer) e.getSender()).getName() + " doesn't exists in the database");
                }
            }
        }
    }

    @EventHandler(priority = 126)
    public void onChat2(ChatEvent e) { // flood
        if (e.isCancelled() || e.isCommand()) {
            return;
        }
        if (e.getSender() instanceof ProxiedPlayer) {
            if (((ProxiedPlayer) e.getSender()).hasPermission("matrix.admin")) {
                return;
            }
            if (Stream.of(disabledServers).anyMatch(server -> ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server))) {
                return;
            }
            ProxiedPlayer pp = (ProxiedPlayer) e.getSender();
            if (messageCooldown.getIfPresent(pp.getUniqueId()) != null) {
                e.setCancelled(true);
                api.getConfig().getStringList("Messages.Spam.Too Fast").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).forEach(line -> pp.sendMessage(TextComponent.fromLegacyText(line)));
                return;
            }
            messageCooldown.put(pp.getUniqueId(), true);
            String lastMessage = messageEquals.getIfPresent(pp.getUniqueId());
            if (lastMessage != null && compare(lastMessage, e.getMessage())) {
                e.setCancelled(true);
                api.getConfig().getStringList("Messages.Spam.Similar Message").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).forEach(line -> pp.sendMessage(TextComponent.fromLegacyText(line)));
                return;
            }
            messageEquals.put(pp.getUniqueId(), e.getMessage());
        }
    }

    @EventHandler(priority = 125)
    public void onChat3(ChatEvent e) { // lowercase
        if (e.isCancelled()) {
            return;
        }
        if (e.getSender() instanceof ProxiedPlayer) {
            if (Stream.of(disabledServers).anyMatch(server -> ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server))) {
                return;
            }
        }
        int min = 3;
        String msg = e.getMessage();
        if (msg.length() > min) {
            int up = 0;
            int down = 0;
            for (int i = 0; i < msg.length(); i++) {
                char character = msg.charAt(i);
                if (Character.isLetter(character)) {
                    if (Character.isUpperCase(character)) {
                        up++;
                    } else {
                        down++;
                    }
                }
            }
            if (down + up != 0) {
                double Percent = 1.0D * up / (up + down) * 100.0D;
                int configpercent = 60;
                if (Percent > configpercent) {
                    e.setMessage(e.getMessage().toLowerCase());
                }
            }
        }
    }

    @EventHandler(priority = 120)
    public void onChat4(ChatEvent e) { // spam
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        if (e.getMessage().matches("[\\uff01-\\uff5e]+")) {
            e.setCancelled(true);
            api.getPlugin().getConsole().execute("ban " + ((ProxiedPlayer) e.getSender()).getName() + " [GGK] Uso de cliente hack");
            return;
        }
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (((ProxiedPlayer) e.getSender()).hasPermission("matrix.helper")) {
            return;
        }
        if (Stream.of(disabledServers).anyMatch(server -> ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server))) {
            return;
        }
        if (e.isCommand() && api.getConfig().getStringList("AntiSpam.Commands Whitelist").stream().anyMatch(command -> e.getMessage().split(" ")[0].replaceFirst("/", "").equalsIgnoreCase(command))) {
            return;
        }
        if (SpamUtils.checkSpam(e.getMessage())) {
            e.setCancelled(true);
            api.getConfig().getStringList("Messages.Spam.Detected").forEach(line -> ((ProxiedPlayer) e.getSender()).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%word%", e.getMessage())))));
            broadcast((ProxiedPlayer) e.getSender(), e.getMessage(), BroadcastType.SPAM);
            return;
        }
        String censoring = checkSpam("AntiSpam.Censored", e.getMessage());
        if (censoring != null) {
            e.setCancelled(true);
            api.getConfig().getStringList("Messages.Spam.Detected").forEach(line -> ((ProxiedPlayer) e.getSender()).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%word%", e.getMessage().replaceAll(e.getMessage().replaceAll(censoring, ""), ""))))));
            broadcast((ProxiedPlayer) e.getSender(), e.getMessage(), BroadcastType.SPAM);
        }
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e) {
        if (e.getTag().equals("matrix:spam")) {
            JsonObject message = Matrix.GSON.fromJson(ByteStreams.newDataInput(e.getData()).readUTF(), JsonObject.class);
            ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(message.get("player").getAsString());
            String spam = message.get("message").getAsString();
            broadcast(pp, spam, BroadcastType.SPAM);
        }
    }

    @EventHandler
    public void onChatEvent(ChatEvent e) {
        if (e.isCommand()) {
            return;
        }
        Connection sender = e.getSender();
        if (sender instanceof ProxiedPlayer) {
            MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId());
            if (matrixPlayer.getStaffChannel() != null) {
                e.setCancelled(true);
                ProxyServer.getInstance().getPluginManager().dispatchCommand((CommandSender) sender, matrixPlayer.getStaffChannel() + " " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = 127)
    public void onBlockedCommand(ChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!e.isCommand()) {
            return;
        }
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (Matrix.getAPI().getPlayer(((ProxiedPlayer) e.getSender()).getName()).isAdmin()) {
            return;
        }
        String command = e.getMessage().toLowerCase().replaceFirst("/", "").split(" ", 1)[0];
        if (command.split(":").length > 1) {
            e.setMessage(e.getMessage().replaceFirst(command.split(":")[0] + ":", ""));
            command = command.split(":")[1];
        }
        if (Arrays.asList(blockedCommands).contains(command)) {
            e.setCancelled(true);
        }
    }

    private String checkCensoring(String message) {
        return checkSpam("Censoring.words", message);
    }

    private String checkSpam(String path, String message) {
        message = message.toLowerCase();
        for (String word : api.getConfig().getStringList(path)) {
            if (message.equalsIgnoreCase(word)) {
                return word;
            }
            if (message.contains(word)) {
                return word;
            }
            if (message.matches(word)) {
                return word;
            }
            Pattern pattern = Pattern.compile("^" + word + "$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(0);
            }
            String[] words = Stream.of(message.replace(".", " ").split(" ")).map(this::normalize).toArray(String[]::new);
            if (words.length > 1) {
                for (String w : words) {
                    String cs = checkSpam(path, w);
                    if (cs != null) {
                        return cs;
                    }
                }
            }
        }
        return null;
    }

    private void broadcast(ProxiedPlayer spamer, String message, BroadcastType broadcastType) {
        ProxyServer.getInstance().getPlayers().stream().filter(p -> p.hasPermission("matrix.helper") && !p.getServer().getInfo().getName().contains("Auth")).forEach(p -> {
            p.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&8&m--------------------&R &c&lANTISPAM &8&M-------------------")));
            p.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&7El jugador &b" + spamer.getName() + " &r&7 ha dicho algo inapropiado")));
            p.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&7Tipo: " + broadcastType)));
            String svname = spamer.getServer().getInfo().getName();
            TextComponent msg = new TextComponent(StringUtils.replace("&c&l(!) &r&7Servidor: &e" + svname + " &7(Click para ir)"));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/btp " + svname));
            p.sendMessage(msg);
            p.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&7Mensaje: &e" + message)));
            p.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&8&m--------------------------------------------------")));
        });
        if (broadcastType == BroadcastType.SPAM) {
            MatrixPlayer matrixPlayer = api.getPlayer(spamer.getUniqueId());
            if (matrixPlayer != null) {
                matrixPlayer.incrSpammingLevel();
                int level = matrixPlayer.getSpammingLevel();
                spamPunishments.get(spamPunishments.containsKey(level) ? level : spamPunishments.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()).get(0)).forEach(k -> ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), k.replace("%name%", spamer.getName()).replace("%message%", message).replace("%word%", message).replace("%count%", String.valueOf(level))));
            }
        }
    }

    private String normalize(String string) {
        return string.toLowerCase().replaceAll("[^\\w]", "");
    }

    private boolean compare(String oldMessage, String newMessage) {
        if (Objects.equals(normalize(oldMessage), "") || Objects.equals(normalize(newMessage), "")) {
            return false;
        }
        return normalize(newMessage).contains(normalize(oldMessage)) && Math.max(normalize(newMessage).length(), normalize(oldMessage).length()) - Math.min(normalize(newMessage).length(), normalize(oldMessage).length()) <= 3;
    }

    private enum BroadcastType {
        CENSORING,
        SPAM
    }
}
