package io.github.beelzebu.matrix.listener;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.channels.Channel;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PubSubMessageListener implements Listener {

    @EventHandler
    public void onMessage(PubSubMessageEvent e) {
        if (e.getChannel().equals("NifheimHelpop") || e.getChannel().equals("Channel") || e.getChannel().equals("Maintenance")) {
            MatrixAPI.getInstance().getMethods().runAsync(() -> {
                if (e.getChannel().equals("NifheimHelpop")) {
                    ProxyServer.getInstance().getPlayers().stream().filter((pp) -> (pp.hasPermission("matrix.staff.aprendiz"))).forEachOrdered((pp) -> {
                        pp.sendMessage(e.getMessage());
                    });
                    ProxyServer.getInstance().getConsole().sendMessage(e.getMessage());
                } else if (e.getChannel().equals("Channel")) {
                    String[] args = e.getMessage().split(" -div- ");
                    for (Channel channel : Main.getChannels()) {
                        if (e.getMessage().startsWith("set ") && channel.getName().equals(e.getMessage().split(",")[1])) {
                            ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(UUID.fromString(e.getMessage().split(",")[2]));
                            Main.setChannelFor(UUID.fromString(e.getMessage().split(",")[2]), channel);
                            if (pp != null) {
                                if (Main.getChannelFor(pp.getUniqueId()) != null && Main.getChannelFor(pp.getUniqueId()).equals(channel)) {
                                    pp.sendMessage(new ComponentBuilder("Ahora todos tus mensajes serán enviados al canal: ").color(ChatColor.YELLOW).append(channel.getName()).color(ChatColor.RED).create());
                                }
                            }
                            break;
                        }
                        if (channel.getName().equals(args[0])) {
                            ProxyServer.getInstance().getPlayers().stream().filter((pp) -> (pp.hasPermission(channel.getPermission()))).forEachOrdered((pp) -> {
                                pp.sendMessage(TextComponent.fromLegacyText("§8[§4" + channel.getName().substring(0, 1) + "§8] §7" + (args[1].contains(",") ? "§8[§6" + args[1].split(",")[0] + "§8]" + " §7" + args[1].split(",")[1] : args[1]) + "§f: " + e.getMessage().substring((args[0] + args[1]).length() + 14)));
                            });
                            ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§8[§c" + channel.getName().substring(0, 1) + "§8] §7" + (args[1].contains(",") ? "§8[§6" + args[1].split(",")[0] + "§8]" + " §7" + args[1].split(",")[1] : args[1]) + "§f: " + e.getMessage().substring((args[0] + args[1]).length() + 14)));
                            break;
                        }
                    }
                } else if (e.getChannel().equalsIgnoreCase("Maintenance")) {
                    if (e.getMessage().equals("switch")) {
                        Main.getInstance().setMaintenance(!Main.getInstance().isMaintenance());
                        if (Main.getInstance().isMaintenance()) {
                            ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
                                Iterator<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers().iterator();
                                while (players.hasNext()) {
                                    players.next().disconnect("");
                                }
                            }, 500, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });
        }
    }
}
