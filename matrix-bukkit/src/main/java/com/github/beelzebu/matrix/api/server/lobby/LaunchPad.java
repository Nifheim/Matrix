package com.github.beelzebu.matrix.api.server.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @author Beelzebu
 */
public class LaunchPad {

    private final Location location;
    private final Vector vector;
    private final boolean effect;

    public LaunchPad(Location location, Vector vector, boolean effect) {
        this.location = location;
        this.vector = vector;
        this.effect = effect;
    }

    public static LaunchPad fromString(String launchpad) {
        String[] args = launchpad.split(",");
        if (args.length == 8) {
            Location location = new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
            Vector vector = new Vector(Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
            boolean effect = false;
            try {
                effect = Boolean.parseBoolean(args[7]);
            } catch (Exception ex) {
            }
            return new LaunchPad(location, vector, effect);
        }
        return null;
    }

    @Override
    public String toString() {
        String launchpad = "";
        launchpad += location.getWorld().getName() + ",";
        launchpad += location.getX() + ",";
        launchpad += location.getY() + ",";
        launchpad += location.getZ() + ",";
        launchpad += vector.getX() + ",";
        launchpad += vector.getY() + ",";
        launchpad += vector.getZ() + ",";
        launchpad += effect;
        return launchpad;
    }

    public Location getLocation() {
        return location;
    }

    public Vector getVector() {
        return vector;
    }

    public boolean isEffect() {
        return effect;
    }
}
