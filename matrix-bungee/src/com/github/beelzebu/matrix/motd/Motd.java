package com.github.beelzebu.matrix.motd;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.countdown.Countdown;
import java.util.List;

/**
 * @author Beelzebu
 */
public class Motd {

    private final String id;
    private final List<String> lines;
    private String countdown;

    public Motd(String id, List<String> lines, String countdown) {
        this.id = id;
        this.lines = lines;
        this.countdown = countdown;
        if (getCountdown() == null) {
            Matrix.getLogger().debug("&7Countdown &6" + countdown + "&7 for motd &6" + id + "&7 doesn't exists.");
        }
    }

    public Motd(String id, List<String> lines) {
        this.id = id;
        this.lines = lines;
    }

    public Countdown getCountdown() {
        return countdown == null ? null : MotdManager.getCountdown(countdown);
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    public String getId() {
        return id;
    }

    public List<String> getLines() {
        return lines;
    }

    public String toString() {
        return "Motd(id=" + id + ", lines=" + lines + ", countdown=" + getCountdown() + ")";
    }

}