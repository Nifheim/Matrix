package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.database.SQLDatabase;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.database.sql.SQLQuery;
import com.github.beelzebu.matrix.util.Throwing;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
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
    public void addFailedLogin(UUID uniqueId, String server, String message) {
        makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_LOGIN.getQuery())) {
                preparedStatement.setString(1, uniqueId.toString());
                preparedStatement.setString(2, trimServerName(server));
                preparedStatement.setString(3, message.length() > 1000 ? message.substring(0, 999) : message);
                preparedStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public Future<Void> incrStat(MatrixPlayer matrixPlayer, String server, Statistic stat, long value) {
        if (value == 0) {
            return CompletableFuture.completedFuture(null);
        }
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
                setDefaultStatsParams(callableStatement, matrixPlayer.getId(), server);
                setStatParam(callableStatement, stat, value);
                callableStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    @Deprecated
    public Future<Void> incrStat(UUID uniqueId, String server, Statistic stat, long value) {
        return incrStat(Matrix.getAPI().getPlayer(uniqueId), server, stat, value);
    }

    @Override
    public Future<Void> incrStats(MatrixPlayer matrixPlayer, String server, Map<Statistic, Long> stats) {
        if (stats.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
                setDefaultStatsParams(callableStatement, matrixPlayer.getId(), server);
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
    @Deprecated
    public Future<Void> incrStats(UUID uniqueId, String server, Map<Statistic, Long> stats) {
        return incrStats(Matrix.getAPI().getPlayer(uniqueId), server, stats);
    }

    @Override
    public Future<Void> insertCommandLogEntry(MatrixPlayer matrixPlayer, String server, String command) {
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_COMMAND_LOG.getQuery())) {
                preparedStatement.setString(1, matrixPlayer.getId());
                preparedStatement.setString(2, server);
                preparedStatement.setString(3, command);
                preparedStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    @Deprecated
    public Future<Void> insertCommandLogEntry(UUID uniqueId, String server, String command) {
        return insertCommandLogEntry(Matrix.getAPI().getPlayer(uniqueId), server, command);
    }

    @Override
    public Future<Long> getStat(MatrixPlayer matrixPlayer, String server, Statistic statistic) {
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
                    preparedStatement.setString(1, matrixPlayer.getId());
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

    @Override
    @Deprecated
    public Future<Long> getStat(UUID uniqueId, String server, Statistic statistic) { // TODO: check switch performance impact
        return getStat(Matrix.getAPI().getPlayer(uniqueId), server, statistic);
    }

    @Override
    public Future<Void> insertPlayStats(MatrixPlayer matrixPlayer, GameType gameType, long playTime) {
        Objects.requireNonNull(matrixPlayer, "matrixPlayer can't be null");
        if (gameType == GameType.NONE || playTime == 0) {
            return CompletableFuture.completedFuture(null);
        }
        return makeFuture(() -> {
            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareCall(SQLQuery.INSERT_PLAY_STATS.getQuery())) {
                preparedStatement.setString(1, matrixPlayer.getId());
                preparedStatement.setString(2, gameType.toString());
                preparedStatement.setLong(3, playTime);
                preparedStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public Future<PlayStats> getPlayStats(MatrixPlayer matrixPlayer, GameType gameType) {
        Objects.requireNonNull(matrixPlayer, "matrixPlayer can't be null");
        return makeFuture(() -> {
            PlayStats playStats = null;
            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareCall(SQLQuery.SELECT_PLAY_STATS.getQuery())) {
                preparedStatement.setString(1, matrixPlayer.getId());
                preparedStatement.setString(2, gameType.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        long joins = resultSet.getLong(1);
                        long totalPlayTime = resultSet.getLong(1);
                        playStats = new PlayStats(joins, totalPlayTime, gameType);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return playStats;
        });
    }

    private void setDefaultStatsParams(CallableStatement callableStatement, String id, String server) throws SQLException {
        callableStatement.setString(1, id);
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
}
