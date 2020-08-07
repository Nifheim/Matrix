package com.github.beelzebu.matrix.command;

import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.PermsUtils;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.stream.Collectors;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PlayerInfoCommand extends Command implements TabExecutor {

    private final MatrixBungeeBootstrap bootstrap;

    public PlayerInfoCommand(MatrixBungeeBootstrap bootstrap) {
        super("playerinfo", "matrix.command.pinfo", "pinfo", "lookup");
        this.bootstrap = bootstrap;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        bootstrap.getApi().getPlugin().runAsync(() -> {
            if (args.length == 0) {
                sender.sendMessage(StringUtils.replace("%prefix% &6Por favor usa &e/" + getName() + " <nombre>"));
            } else {
                if (bootstrap.getApi().getDatabase().isRegistered(args[0])) {
                    MatrixPlayer player = bootstrap.getApi().getPlayer(args[0]);
                    if (args.length >= 2 && args[1].equalsIgnoreCase("json")) {
                        sender.sendMessage(TextComponent.fromLegacyText(new GsonBuilder().setPrettyPrinting().create().toJson(player, MongoMatrixPlayer.class)));
                    } else {
                        BaseComponent[] msg = TextComponent.fromLegacyText(StringUtils.replace(
                                "%prefix% Información de &c" + player.getName() + "&r\n"
                                        + " \n"
                                        + " &cUUID &8• &7" + player.getUniqueId() + "&r\n"
                                        + " &cDisplay name &8• &7" + player.getDisplayName() + "&r\n"
                                        + " &cLevel &8• &7" + player.getLevel() + "&r\n"
                                        + " &cExperience &8• &7" + player.getExp() + "&r\n"
                                        + " &cCoins &8• &7" + player.getCoins() + "&r\n"
                                        + " &cRank &8• &7" + (PermsUtils.getPrefix(player.getUniqueId()).length() < 3 ? "default" : PermsUtils.getPrefix(player.getUniqueId())) + "&r\n"
                                        + " &cChatColor &8• &7" + player.getChatColor() + "&r\n"
                                        + " &cDiscord Id &8• &7" + (player.getDiscordId() != null ? player.getDiscordId() : "Not associated") + "&r\n"
                                        + " &cVanished &8• &7" + player.isVanished() + "&r\n"
                                        + " &cLast GameType &8• &7" + player.getLastGameType() + "&r\n"
                                        + " &cLast Locale &8• &7" + player.getLastLocale() + "&r\n"
                                        + " &cAdmin &8• &7" + player.isAdmin() + "&r\n"
                                        + " &cStaff Channel &8• &7" + (player.getStaffChannel() != null ? player.getStaffChannel() : "none") + "&r\n"
                                        + " &cLast login &8• &7" + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(player.getLastLogin().getTime()) + "&r\n"
                                        + " &cRegistered &8• &7" + player.isRegistered() + "\n"
                                        + " &cPremium &8• &7" + player.isPremium() + "\n"
                                        + " &cLoggedIn &8• &7" + player.isLoggedIn() + "\n"
                                        + " &cRegistration &8• &7" + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(player.getRegistration().getTime()) + "&r\n"
                                        //+ " &cBaneado &8• &7" + (ban != null ? "si" : "no") + "&r\n"
                                        //+ (ban != null ? "   &cRazón &8• &7" + ban.getReason() + "&r\n" : "")
                                        + " &cIP &8• &7" + (player.getIP() != null ? player.getIP() : (player.getIpHistory().stream().findFirst().isPresent() ? player.getIpHistory().stream().findFirst().get() : "no registrada")) + "&r\n"
                                        + " &cIP History &8• &r\n" + createList(player.getIpHistory())
                        ));
                        sender.sendMessage(msg);
                    }
                } else {
                    sender.sendMessage(StringUtils.replace("%prefix% No se ha encontrado a " + args[0] + " en la base de datos."));
                }
            }
        });
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return ProxyServer.getInstance().getPlayers().stream().filter(proxiedPlayer -> proxiedPlayer.getName().toLowerCase().startsWith(args.length >= 1 ? args[0] : "")).map(ProxiedPlayer::getName).collect(Collectors.toSet());
    }

    private String createList(Collection<String> collection) {
        StringBuilder list = new StringBuilder();
        collection.forEach(entry -> list.append(StringUtils.replace("  &f- &7" + entry + "\n")));
        return list.toString();
    }
}
