package io.github.beelzebu.matrix.api.logging;

import io.github.beelzebu.matrix.api.Matrix;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class MatrixLogger {

    private static final String PREFIX = "&8[&cMatrix&8] &7";

    public void log(Level level, String msg) {
        if (level.intValue() <= Level.FINE.intValue() && Matrix.getAPI().getConfig().getBoolean("Debug")) {
            Matrix.getAPI().getPlugin().getConsole().sendMessage(PREFIX + "&cDebug: &7" + msg);
        } else {
            Matrix.getAPI().getPlugin().getConsole().sendMessage(PREFIX + msg);
        }
    }

    public void log(String msg) {
        log(Level.INFO, msg);
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void debug(String msg) {
        log(Level.FINEST, msg);
    }

    public final void debug(SQLException ex) {
        log("SQLException: ");
        log("   Database state: " + ex.getSQLState());
        log("   Error code: " + ex.getErrorCode());
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    public final void debug(JedisException ex) {
        log("JedisException: ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    public final void debug(Exception ex) {
        log(ex.getClass().getName() + ": ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    private String getStacktrace(Exception ex) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            ex.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error getting the stacktrace";
    }

}
