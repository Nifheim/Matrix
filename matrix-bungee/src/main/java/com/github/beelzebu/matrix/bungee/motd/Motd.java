package com.github.beelzebu.matrix.bungee.motd;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.bungee.countdown.Countdown;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class Motd {

    private final String id;
    private final @NotNull List<String> lines;
    private String countdown;

    public Motd(String id, @NotNull List<String> lines, String countdown) {
        if (lines.size() == 0) {
            throw new IllegalArgumentException("Can't create a motd without lines.");
        }
        this.id = Objects.requireNonNull(id, "id can't be null.");
        this.lines = lines;
        this.countdown = countdown;
        if (getCountdown() == null) {
            Matrix.getLogger().debug("&7Countdown &6" + countdown + "&7 for motd &6" + id + "&7 doesn't exists.");
        }
    }

    public Motd(String id, @NotNull List<String> lines) {
        this(id, lines, null);
    }

    public @Nullable Countdown getCountdown() {
        return countdown == null ? null : MotdManager.getCountdown(countdown);
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    public String getId() {
        return id;
    }

    public @NotNull List<String> getLines() {
        return lines;
    }

    public @NotNull String toString() {
        return "Motd(id=" + id + ", lines=" + lines + ", countdown=" + getCountdown() + ")";
    }

}