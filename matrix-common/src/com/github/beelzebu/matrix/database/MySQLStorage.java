package com.github.beelzebu.matrix.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.database.SQLDatabase;
import com.github.beelzebu.matrix.api.player.Statistic;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Beelzebu
 */
public class MySQLStorage implements SQLDatabase {

    private HikariDataSource dataSource;

    public MySQLStorage(String host, int port, String database, String user, String password) {
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("Matrix MySQL Connection Pool");
        hc.setDriverClassName("org.mariadb.jdbc.Driver");
        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("useServerPrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hc.addDataSourceProperty("encoding", "UTF-8");
        hc.addDataSourceProperty("characterEncoding", "utf8");
        hc.addDataSourceProperty("useUnicode", "true");
        hc.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false");
        hc.setUsername(user);
        hc.setPassword(password);
        hc.setMaxLifetime(60000);
        hc.setMinimumIdle(4);
        hc.setIdleTimeout(30000);
        hc.setConnectionTimeout(10000);
        hc.setMaximumPoolSize(12);
        hc.setLeakDetectionThreshold(30000);
        hc.validate();
        try {
            dataSource = new HikariDataSource(hc);
        } catch (Exception ex) {
            Matrix.getLogger().info("An exception has occurred while starting connection pool, check your database credentials.");
            Matrix.getLogger().debug(ex);
        }
    }

    @Override
    public void addFailedLogin(UUID uuid, String message) {

    }

    @Override
    public Future<Double> incrStat(UUID uuid, String server, String stat, double value) {
        // TODO complete
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = dataSource.getConnection(); ResultSet res = c.prepareStatement("").executeQuery()) {
                return 1D;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return 0D;
        });
    }

    @Override
    public Future<Double> incrStat(UUID uuid, String server, Statistic stat, double value) {
        return null;
    }
}
