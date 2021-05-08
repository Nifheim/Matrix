package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.nifheim.bukkit.util.command.MatrixCommand;
import org.bukkit.command.CommandSender;

public class PlayerInfoCommand extends MatrixCommand {

    public PlayerInfoCommand() {
        super("playerinfo", "matrix.command.pinfo", "pinfo", "lookup");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        api.getPlugin().getBootstrap().getScheduler().executeAsync(() -> {
            if (args.length == 0) {
                sender.sendMessage(StringUtils.replace("%prefix% &6Por favor usa &e/" + getName() + " <nombre>"));
            } else {
                MatrixPlayer player = api.getPlayerManager().getPlayerByName(args[0]).join();
                if (player != null) {
                    if (args.length >= 2 && args[1].equalsIgnoreCase("json")) {
                        sender.sendMessage(TextComponent.fromLegacyText(new GsonBuilder().setPrettyPrinting().create().toJson(player, MongoMatrixPlayer.class)));
                    } else {
                        BaseComponent[] msg = TextComponent.fromLegacyText(StringUtils.replace(
                                "%prefix% Información de &c" + player.getName() + "&r\n"
                                        + " \n"
                                        + " &cUUID &8• &7" + player.getUniqueId() + "&r\n"
                                        + " &cDisplay name &8• &7" + player.getDisplayName() + "&r\n"
                                        + " &cDiscord Id &8• &7" + (player.getDiscordId() != null ? player.getDiscordId() : "Not associated") + "&r\n"
                                        + " &cVanished &8• &7" + player.isVanished() + "&r\n"
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

    private String createList(Collection<String> collection) {
        StringBuilder list = new StringBuilder();
        collection.forEach(entry -> list.append(StringUtils.replace("  &f- &7" + entry + "\n")));
        return list.toString();
    }
}
