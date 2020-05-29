package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.motd.Motd;
import io.github.beelzebu.matrix.motd.MotdManager;
import java.util.UUID;
import java.util.stream.Stream;
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

    public ServerListListener() {
    }

    @EventHandler(priority = 127)
    public void onPing(ProxyPingEvent e) {
        e.registerIntent((Plugin) Matrix.getAPI().getPlugin().getBootstrap());
        Matrix.getAPI().getPlugin().runAsync(() -> {
            try {
                // TODO: rehabilitar
                /*if (e.getConnection().getVirtualHost() != null) {
                    String host = e.getConnection().getVirtualHost().getHostName();
                    if (!Objects.equals(host, Matrix.IP)) {
                        e.getResponse().setDescriptionComponent(new TextComponent("Please join using " + Matrix.IP + "\nPor favor ingresa usando " + Matrix.IP));
                        return;
                    }
                    int port = e.getConnection().getVirtualHost().getPort();
                    if (!Objects.equals(port, 25565)) {
                        e.getResponse().setDescriptionComponent(new TextComponent("Please join using " + Matrix.IP + "\nPor favor ingresa usando " + Matrix.IP));
                        return;
                    }
                }
                 */
                if (((MatrixBungeeBootstrap) Matrix.getAPI().getPlugin().getBootstrap()).isMaintenance()) {
                    e.getResponse().getVersion().setProtocol(-1);
                    e.getResponse().getVersion().setName("§cEn mantenimiento");
                }
                // select random motd
                Motd motd = MotdManager.getRandomMotd();
                if (motd != null) {
                    String s = motd.getLines().get(0) + "\n" + motd.getLines().get(1);
                    TextComponent tc = new TextComponent();
                    Stream.of(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', s).replace("%countdown%", motd.getCountdown() != null ? motd.getCountdown().getCountdown() : ""))).forEach(tc::addExtra);
                    e.getResponse().setDescriptionComponent(tc);
                }
                // set player hover
                ServerPing.PlayerInfo[] playerInfos = new ServerPing.PlayerInfo[playerHover.length];
                for (int i = 0; i < playerHover.length; i++) {
                    playerInfos[i] = new ServerPing.PlayerInfo(StringUtils.replace(playerHover[i]), emptyUUID);
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