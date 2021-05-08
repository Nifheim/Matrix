package com.github.beelzebu.matrix.bungee.influencer;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class InfluencerManager {

    private static final String INFLUENCERS_CONFIG_KEY = "Influencers";
    private final MatrixBungeeBootstrap bootstrap;
    private final Set<Influencer> influencers = new HashSet<>();

    public InfluencerManager(MatrixBungeeBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void loadInfluencers() {
        Set<String> influencersKeys = new HashSet<>(bootstrap.getConfig().getKeys(INFLUENCERS_CONFIG_KEY));
        if (influencersKeys.isEmpty()) {
            return;
        }
        for (String influencerKey : influencersKeys) {
            String path = INFLUENCERS_CONFIG_KEY + "." + influencerKey + ".";
            String name = bootstrap.getConfig().getString(path + "Name");
            InfluencerType type = null;
            try {
                type = InfluencerType.valueOf(bootstrap.getConfig().getString(path + "Type").toUpperCase());
            } catch (Exception e) {
                Matrix.getLogger().info("Invalid InfluencerType for '" + name + "'");
                Matrix.getLogger().info("Valid types are: " + Arrays.toString(InfluencerType.values()));
                e.printStackTrace();
            }
            if (type == null) {
                continue;
            }
            String socialNetworkLink = bootstrap.getConfig().getString(path + "SocialNetwork");
            Influencer influencer = new Influencer(name, type, socialNetworkLink);
            createCommand(influencer);
            influencers.add(influencer);
        }
    }

    public void reloadInfluencers() {
        for (Influencer influencer : influencers) {
            if (influencer.getCommand() == null) {
                continue;
            }
            ProxyServer.getInstance().getPluginManager().unregisterCommand(influencer.getCommand());
        }
        influencers.clear();
        loadInfluencers();
    }

    public void createCommand(Influencer influencer) {
        Command command = new Command(influencer.getName()) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                // TODO: update command content
                sender.sendMessage(influencer.getSocialNetwork());
            }
        };
        ProxyServer.getInstance().getPluginManager().registerCommand(bootstrap, command);
        influencer.setCommand(command);
    }

    public Set<Influencer> getInfluencers() {
        return ImmutableSet.copyOf(influencers);
    }
}
