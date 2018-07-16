package io.github.beelzebu.matrix.api.server.lobby;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
@AllArgsConstructor
public class LaunchPad {

    private final Location location;
    private final Vector vector;
    private final boolean effect;

    public static LaunchPad fromString(String launchpad) {
        String[] args = launchpad.split(",");
        if (args.length == 7) {
            Location l = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
            Vector v = new Vector(Double.valueOf(args[4]), Double.valueOf(args[5]), Double.valueOf(args[6]));
            boolean e = false;
            try {
                e = Boolean.valueOf(args[7]);
            } catch (Exception ex) {
            }
            return new LaunchPad(l, v, e);
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
        launchpad += vector.getZ();
        return launchpad;
    }
}
