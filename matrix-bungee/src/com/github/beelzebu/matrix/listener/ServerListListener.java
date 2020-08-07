package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.motd.Motd;
import com.github.beelzebu.matrix.motd.MotdManager;
import java.util.UUID;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
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
    private final String[] playerHover;

    public ServerListListener(String[] playerHover) {
        this.playerHover = playerHover;
    }

    @EventHandler(priority = 127)
    public void onPing(ProxyPingEvent e) {
        e.registerIntent((Plugin) Matrix.getAPI().getPlugin().getBootstrap());
        Matrix.getAPI().getPlugin().runAsync(() -> {
            try {
                if (e.getConnection().getVirtualHost() == null) {
                    e.getResponse().setDescriptionComponent(new TextComponent("Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\nPor favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                    return;
                }
                String host = e.getConnection().getVirtualHost().getHostName();
                if (host == null) {
                    e.getResponse().setDescriptionComponent(new TextComponent("Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\nPor favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                    return;
                }
                boolean badDomain = true;
                for (String domain : MatrixAPIImpl.DOMAIN_NAMES) {
                    if (host.endsWith(domain)) {
                        badDomain = false;
                        break;
                    }
                }
                if (badDomain) {
                    e.getResponse().setDescriptionComponent(new TextComponent("Please join using " + MatrixAPIImpl.DOMAIN_NAME + "\nPor favor ingresa usando " + MatrixAPIImpl.DOMAIN_NAME));
                    return;
                }
                if (((MatrixAPIImpl) Matrix.getAPI()).getMaintenanceManager().isMaintenance()) {
                    e.getResponse().getVersion().setProtocol(-1);
                    e.getResponse().getVersion().setName("§cEn mantenimiento");
                }
                // select random motd
                Motd motd = MotdManager.getForcedMotd(host);
                if (motd == null) {
                    motd = MotdManager.getRandomMotd();
                }
                String s = motd.getLines().get(0) + "\n" + motd.getLines().get(1);
                TextComponent tc = new TextComponent();
                for (BaseComponent line : TextComponent.fromLegacyText(StringUtils.replace(s.replace("%countdown%", motd.getCountdown() != null ? motd.getCountdown().getCountdown() : "")))) {
                    tc.addExtra(line);
                }
                e.getResponse().setDescriptionComponent(tc);
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