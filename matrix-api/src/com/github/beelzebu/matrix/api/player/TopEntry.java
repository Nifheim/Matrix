package com.github.beelzebu.matrix.api.player;

/**
 * @author Beelzebu
 */
public class TopEntry {

    private final String id;
    private final String name;
    private final long value;
    private final long position;

    public TopEntry(String id, String name, long value, long position) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public long getPosition() {
        return position;
    }
}
