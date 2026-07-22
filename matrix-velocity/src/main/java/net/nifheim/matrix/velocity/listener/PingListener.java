package net.nifheim.matrix.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.ServerPing;

public class PingListener {

    private static final String PROTOCOL_VERSION_NAME = "Nifheim 26.2";
    private static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.MINECRAFT_26_2;

    public PingListener() {
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getPing();
        int version = event.getConnection().getProtocolVersion().getProtocol();
        if (version > PROTOCOL_VERSION.getProtocol()) {
            ping = ping.asBuilder().version(new ServerPing.Version(version, PROTOCOL_VERSION_NAME)).build();
        } else {
            ping = ping.asBuilder().version(new ServerPing.Version(PROTOCOL_VERSION.getProtocol(), PROTOCOL_VERSION_NAME)).build();
        }
        event.setPing(ping);
    }
}
