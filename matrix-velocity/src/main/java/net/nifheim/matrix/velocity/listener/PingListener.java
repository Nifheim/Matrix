package net.nifheim.matrix.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.ServerPing;

public class PingListener {

    public PingListener() {
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getPing();
        ping = ping.asBuilder().version(new ServerPing.Version(ProtocolVersion.MINECRAFT_1_21.getProtocol(), "Nifheim 1.21")).build();
        event.setPing(ping);
    }
}
