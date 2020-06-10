package com.github.beelzebu.matrix.logger;

import com.github.beelzebu.matrix.api.command.CommandSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public final class MatrixLoggerImpl implements MatrixLogger {

    private static final String PREFIX = "&8[&cMatrix&8] &7";
    private final CommandSource console;
    private final boolean debug;

    public MatrixLoggerImpl(CommandSource console, boolean debug) {
        this.console = Objects.requireNonNull(console, "Console can't be null.");
        this.debug = debug;
    }

    @Override
    public void log(Level level, String msg) {
        Objects.requireNonNull(msg);
        if (level.intValue() <= Level.FINE.intValue()) {
            if (!debug) {
                return;
            }
            console.sendMessage(PREFIX + "&cDebug: &7" + msg);
        } else {
            console.sendMessage(PREFIX + msg);
        }
    }

    @Override
    public void log(String msg) {
        log(Level.INFO, msg);
    }

    @Override
    public void info(String msg) {
        log(Level.INFO, msg);
    }

    @Override
    public void debug(String msg) {
        log(Level.FINEST, msg);
    }

    @Override
    public void debug(SQLException ex) {
        log("SQLException: ");
        log("   Database state: " + ex.getSQLState());
        log("   Error code: " + ex.getErrorCode());
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    public void debug(JedisException ex) {
        log("JedisException: ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    @Override
    public void debug(Exception ex) {
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
