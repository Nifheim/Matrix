package com.github.beelzebu.matrix.influencer;

import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class Influencer {

    private final String name;
    private final InfluencerType type;
    private final String socialNetwork;
    private Command command;

    public Influencer(String name, InfluencerType type, String socialNetwork) {
        this.name = name;
        this.type = type;
        this.socialNetwork = socialNetwork;
    }

    public String getName() {
        return name;
    }

    public InfluencerType getType() {
        return type;
    }

    public String getSocialNetwork() {
        return socialNetwork;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
