package io.github.beelzebu.matrix.motd;

import io.github.beelzebu.matrix.countdown.Countdown;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Beelzebu
 */
@Data
@AllArgsConstructor
public class Motd {

    private final String id;
    private final List<String> lines;
    private String countdown;

    public Countdown getCountdown() {
        return countdown == null ? null : MotdManager.getCountdown(countdown);
    }
}