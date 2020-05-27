package io.github.beelzebu.matrix.countdown;

import java.util.concurrent.TimeUnit;

/**
 * @author Beelzebu
 */
public class Countdown {

    private final String id;
    private final long start;
    private final long end;

    public Countdown(String id, long start, long end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    private static String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long days = TimeUnit.MILLISECONDS.toDays(millis);

        StringBuilder b = new StringBuilder();
        if (days > 0) {
            b.append(days).append("d, ");
        }
        b.append(hours == 0 ? "00" : hours < 10 ? "0" + hours : String.valueOf(hours));
        b.append(":");
        b.append(minutes == 0 ? "00" : minutes < 10 ? "0" + minutes : String.valueOf(minutes));
        b.append(":");
        b.append(seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : String.valueOf(seconds));
        return b.toString();
    }

    public String getCountdown() {
        return formatTime(end - System.currentTimeMillis());
    }

    public boolean isOver() {
        return end < System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}