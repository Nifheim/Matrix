package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.motd.Motd;
import io.github.beelzebu.matrix.motd.MotdManager;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public class ServerListListener implements Listener {

    private final UUID emptyUUID = UUID.fromString("0-0-0-0-0");
    private final String[] playerHover = {
            "&4&lNifheim &c&lMinecraft",
            "&8&m-----------------------------------&f",
            "&f ¡Entra a divertirte con nosotros!",
            "&f",
            "&f Web: &awww.nifheim.net",
            "&f Discord: &awww.nifheim.net/discord",
            "&f Tienda: &awww.nifheim.net/tienda",
            "&f Twitter: &a@NifheimNetwork",
            "&f Facebook: &awww.facebook.com/NifheimNetwork",
            "&8&m-----------------------------------&f"
    };
    private final Random r = new Random();

    @EventHandler(priority = 127)
    public void onPing(ProxyPingEvent e) {
        e.registerIntent((Plugin) Matrix.getAPI().getPlugin().getBootstrap());
        Matrix.getAPI().getPlugin().runAsync(() -> {
            try {
                String host = e.getConnection().getVirtualHost().getHostName();
                if (!Objects.equals(host, "mc.nifheim.net")) {
                    e.getResponse().setDescriptionComponent(new TextComponent("Please join using mc.nifheim.net\nPor favor ingresa usando mc.nifheim.net"));
                    return;
                }
                if (((MatrixBungeeBootstrap) Matrix.getAPI().getPlugin().getBootstrap()).isMaintenance()) {
                    e.getResponse().getVersion().setProtocol(-1);
                    e.getResponse().getVersion().setName("§cEn mantenimiento");
                }
                // select random motd
                List<Motd> motds = MotdManager.getMotds().stream().filter(motd -> motd.getCountdown() == null || (motd.getCountdown() != null && !motd.getCountdown().isOver())).collect(Collectors.toList());
                if (motds.size() > 1 && motds.stream().filter(motd -> motd.getCountdown() != null && !motd.getCountdown().isOver()).count() >= 1) {
                    motds = motds.stream().filter(motd -> motd.getCountdown() != null && !motd.getCountdown().isOver()).collect(Collectors.toList());
                }
                Motd motd = motds.get(r.nextInt(motds.size()));
                String s = motd.getLines().get(0) + "\n" + motd.getLines().get(1);
                TextComponent tc = new TextComponent();
                Stream.of(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', s).replace("%countdown%", motd.getCountdown() != null ? motd.getCountdown().getCountdown() : ""))).forEach(tc::addExtra);
                e.getResponse().setDescriptionComponent(tc);
                // set player hover
                ServerPing.PlayerInfo[] playerInfos = new ServerPing.PlayerInfo[playerHover.length];
                for (int i = 0; i < playerHover.length; i++) {
                    playerInfos[i] = new ServerPing.PlayerInfo(playerHover[i], emptyUUID);
                }
                e.getResponse().getPlayers().setSample(playerInfos);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                e.completeIntent((Plugin) Matrix.getAPI().getPlugin().getBootstrap());
            }
        });
    }
}