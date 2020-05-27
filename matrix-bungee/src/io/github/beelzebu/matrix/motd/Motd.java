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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.motd.Motd)) {
            return false;
        }
        io.github.beelzebu.matrix.motd.Motd other = (io.github.beelzebu.matrix.motd.Motd) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        java.lang.Object this$id = id;
        java.lang.Object other$id = other.id;
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
            return false;
        }
        java.lang.Object this$lines = lines;
        java.lang.Object other$lines = other.lines;
        if (this$lines == null ? other$lines != null : !this$lines.equals(other$lines)) {
            return false;
        }
        java.lang.Object this$countdown = getCountdown();
        java.lang.Object other$countdown = other.getCountdown();
        if (this$countdown == null ? other$countdown != null : !this$countdown.equals(other$countdown)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        java.lang.Object $id = id;
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        java.lang.Object $lines = lines;
        result = result * PRIME + ($lines == null ? 43 : $lines.hashCode());
        java.lang.Object $countdown = getCountdown();
        result = result * PRIME + ($countdown == null ? 43 : $countdown.hashCode());
        return result;
    }

    public String toString() {
        return "Motd(id=" + id + ", lines=" + lines + ", countdown=" + getCountdown() + ")";
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.motd.Motd;
    }
}