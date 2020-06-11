package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.database.SQLDatabase;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.database.sql.SQLQuery;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.Throwing;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

/**
 * @author Beelzebu
 */
public class MySQLStorage implements SQLDatabase {

    private final MatrixPlugin plugin;
    private HikariDataSource dataSource;

    public MySQLStorage(MatrixPlugin plugin, String host, int port, String database, String user, String password, int poolSize) {
        this.plugin = plugin;
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
        hc.setMinimumIdle(1);
        hc.setIdleTimeout(30000);
        hc.setConnectionTimeout(10000);
        hc.setMaximumPoolSize(poolSize);
        hc.setLeakDetectionThreshold(30000);// TODO: check performance impact
        hc.validate();
        try {
            dataSource = new HikariDataSource(hc);
        } catch (Exception ex) {
            Matrix.getLogger().info("An exception has occurred while starting connection pool, check your database credentials.");
            Matrix.getLogger().debug(ex);
        }
    }

    @Override
    public void addFailedLogin(UUID uuid, String server, String message) {
        makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_LOGIN.getQuery())) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, trimServerName(server));
                preparedStatement.setString(3, message.length() > 1000 ? message.substring(0, 999) : message);
                preparedStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public Future<Void> incrStat(UUID uuid, String server, Statistic stat, long value) {
        if (value == 0) {
            return CompletableFuture.completedFuture(null);
        }
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
                setDefaultStatsParams(callableStatement, uuid, server);
                setStatParam(callableStatement, stat, value);
                callableStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public Future<Void> incrStats(UUID uuid, String server, Map<Statistic, Long> stats) {
        if (stats.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
                setDefaultStatsParams(callableStatement, uuid, server);
                for (Map.Entry<Statistic, Long> ent : stats.entrySet()) {
                    Statistic stat = ent.getKey();
                    long value = ent.getValue();
                    setStatParam(callableStatement, stat, value);
                }
                callableStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public Future<Long> getStat(UUID uuid, String server, Statistic statistic) { // TODO: check switch performance impact
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection()) {
                PreparedStatement preparedStatement = null;
                switch (statistic) {
                    case KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS.getQuery());
                        break;
                    case MOB_KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS.getQuery());
                        break;
                    case DEATHS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS.getQuery());
                        break;
                    case BLOCKS_BROKEN:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN.getQuery());
                        break;
                    case BLOCKS_PLACED:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED.getQuery());
                        break;
                }
                if (preparedStatement != null) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setString(2, trimServerName(server));
                    ResultSet res = preparedStatement.executeQuery();
                    if (res.next()) {
                        return res.getLong(1);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return 0L;
        });
    }

    private void setDefaultStatsParams(CallableStatement callableStatement, UUID uniqueId, String server) throws SQLException {
        callableStatement.setString(1, uniqueId.toString());
        callableStatement.setString(2, trimServerName(server));
        callableStatement.setLong(3, 0);
        callableStatement.setLong(4, 0);
        callableStatement.setLong(5, 0);
        callableStatement.setLong(6, 0);
        callableStatement.setLong(7, 0);
    }

    private void setStatParam(CallableStatement callableStatement, Statistic stat, long value) throws SQLException {
        callableStatement.setLong(stat.getId(), value);
    }

    private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }

    private CompletableFuture<Void> makeFuture(Throwing.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }

    private String trimServerName(String server) {
        server = server.toLowerCase().trim();
        if (server.length() > 35) {
            server = server.substring(0, 34);
        }
        return server;
    }

    public void migratePremium() {
        plugin.runAsync(() -> {
            try (Connection c = dataSource.getConnection(); ResultSet res = c.prepareStatement("SELECT * FROM minecraft_auth.premium").executeQuery()) {
                while (res.next()) {
                    UUID uuid = UUID.fromString(res.getString("uniqueId"));
                    if (UUID.nameUUIDFromBytes(("OfflinePlayer:" + res.getString("name")).getBytes()).equals(uuid)) {
                        continue;
                    }
                    MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer(uuid, res.getString("name"));
                    mongoMatrixPlayer.setRegistered(true);
                    mongoMatrixPlayer.setPremium(true);
                    mongoMatrixPlayer.save();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
