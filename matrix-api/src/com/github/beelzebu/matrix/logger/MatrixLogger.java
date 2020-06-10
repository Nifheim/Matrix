package com.github.beelzebu.matrix.logger;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author Beelzebu
 */
public interface MatrixLogger {

    void log(Level level, String msg);

    void log(String msg);

    void info(String msg);

    void debug(String msg);

    void debug(SQLException ex);

    void debug(Exception ex);
}
