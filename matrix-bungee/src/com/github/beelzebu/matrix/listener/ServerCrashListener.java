package com.github.beelzebu.matrix.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
public class ServerCrashListener implements Listener {

    @EventHandler
    public void onServerKick(ServerKickEvent e) {
        String reason = TextComponent.toLegacyText(e.getKickReasonComponent());

    }
}
