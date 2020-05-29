package io.github.beelzebu.matrix.motd;

import io.github.beelzebu.matrix.countdown.Countdown;
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