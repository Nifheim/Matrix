package io.github.beelzebu.matrix.networkxp;

import java.util.List;
import java.util.Set;

public class Reward {

    private final int level;
    private final Set<String> commands;
    private final List<String> messages;

    public Reward(int level, Set<String> commands, List<String> messages) {
        this.level = level;
        this.commands = commands;
        this.messages = messages;
    }

    public int getLevel() {
        return level;
    }

    public Set<String> getCommands() {
        return commands;
    }

    public List<String> getMessages() {
        return messages;
    }
}
