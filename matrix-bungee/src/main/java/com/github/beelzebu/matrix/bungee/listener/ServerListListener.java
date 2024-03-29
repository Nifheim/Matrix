package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bungee.motd.Motd;
import com.github.beelzebu.matrix.bungee.motd.MotdManager;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class ServerListListener implements Listener {

    private final MatrixBungeeBootstrap plugin;
    private final UUID emptyUUID = UUID.fromString("0-0-0-0-0");
    private final LoadingCache<String, Favicon> favicons = Caffeine.newBuilder().weakValues().expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Favicon>() {
        @Override
        public @Nullable Favicon load(@NonNull String key) {
            BufferedImage bufferedImage;
            Matrix.getLogger().debug("Searching favicon: " + key);
            try {
                File imageFile = new File(new File(plugin.getDataFolder(), "favicons"), key + ".png");
                if (!imageFile.exists()) {
                    return null;
                }
                bufferedImage = ImageIO.read(imageFile);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
            if (bufferedImage == null) {
                return null;
            }
            return Favicon.create(bufferedImage);
        }
    });
    private final ServerPing.PlayerInfo[] playerInfos;

    public ServerListListener(MatrixBungeeBootstrap plugin, String[] playerHover) {
        this.plugin = plugin;
        playerInfos = new ServerPing.PlayerInfo[playerHover.length];
        for (int i = 0; i < playerHover.length; i++) {
            playerInfos[i] = new ServerPing.PlayerInfo(ChatColor.translateAlternateColorCodes('&', playerHover[i]), emptyUUID);
        }
    }

    @EventHandler(priority = 127)
    public void onPing(@NotNull ProxyPingEvent e) {
        e.registerIntent((Plugin) Matrix.getAPI().getPlugin().getBootstrap());
        Matrix.getAPI().getPlugin().getBootstrap().getScheduler().executeAsync(() -> {
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
                e.getResponse().getPlayers().setSample(playerInfos);
                String faviconName = host.split("\\.", 2)[1];
                if (!faviconName.contains(".")) {
                    faviconName = host;
                }
                Favicon favicon = favicons.get(faviconName);
                if (favicon != null) {
                    e.getResponse().setFavicon(favicon);
                }
                if (((MatrixAPIImpl) Matrix.getAPI()).getMaintenanceManager().isMaintenance()) {
                    e.getResponse().getVersion().setProtocol(-1);
                    e.getResponse().getVersion().setName("§cEn mantenimiento");
                    return;
                }
                if (e.getConnection().getVersion() < ProtocolConstants.MINECRAFT_1_18) {
                    e.getResponse().getVersion().setProtocol(ProtocolConstants.MINECRAFT_1_18);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                e.completeIntent(plugin);
            }
        });
    }
}