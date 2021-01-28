package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.player.TopEntry;
import com.github.beelzebu.matrix.database.sql.SQLQuery;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.converters.UUIDConverter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class StorageImpl {

    public static final int TOP_SIZE = 10;
    private final Cache<Statistic, TopEntry[]> statsTotalCache = Caffeine.newBuilder().weakValues().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> statsWeeklyCache = Caffeine.newBuilder().weakValues().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> statsMonthlyCache = Caffeine.newBuilder().weakValues().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private HikariDataSource dataSource;
    private final Datastore datastore;

    public StorageImpl(MatrixAPIImpl<?> api) {
        MongoClient client = new MongoClient(new ServerAddress(api.getConfig().getString("Database.Host"), 27017), MongoCredential.createCredential("admin", "admin", api.getConfig().getString("Database.Password").toCharArray()), MongoClientOptions.builder().build());
        Morphia morphia = new Morphia();
        morphia.getMapper().getConverters().addConverter(new UUIDConverter());
        morphia.map(MongoMatrixPlayer.class);
        this.datastore = morphia.createDatastore(client, "matrix");
        this.datastore.ensureIndexes();
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("Matrix MySQL Connection Pool");
        hc.setDriverClassName("com.github.beelzebu.lib.mariadb.Driver");
        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("useServerPrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hc.addDataSourceProperty("encoding", "UTF-8");
        hc.addDataSourceProperty("characterEncoding", "utf8");
        hc.addDataSourceProperty("useUnicode", "true");
        hc.setJdbcUrl("jdbc:mariadb://" + api.getConfig().getString("mysql.host") + ":" + api.getConfig().getInt("mysql.port") + "/" + api.getConfig().getString("mysql.database") + "?autoReconnect=true&useSSL=false");
        hc.setUsername(api.getConfig().getString("mysql.user"));
        hc.setPassword(api.getConfig().getString("mysql.password"));
        hc.setMaxLifetime(60000L);
        hc.setMinimumIdle(1);
        hc.setIdleTimeout(30000L);
        hc.setConnectionTimeout(10000L);
        hc.setMaximumPoolSize(api.getConfig().getInt("mysql.pool", 12));
        hc.validate();

        try {
            this.dataSource = new HikariDataSource(hc);
        } catch (Exception e) {
            Matrix.getLogger().info("An exception has occurred while starting connection pool, check your database credentials.");
            Matrix.getLogger().debug(e);
        }

    }

    public MatrixPlayer getPlayer(UUID uniqueId) {
        return this.datastore.find(MongoMatrixPlayer.class)
                .filter("uniqueId", uniqueId).first();
    }

    public MatrixPlayer getPlayer(String name) {
        return Optional.ofNullable(
                this.datastore.find(MongoMatrixPlayer.class)
                        .filter("lowercaseName", name.toLowerCase()).first())
                .orElse(
                        this.datastore.find(MongoMatrixPlayer.class)
                                .filter("name", name).first());
    }

    public MatrixPlayer getPlayerById(@NotNull String hexId) {
        Matrix.getLogger().info(hexId);
        return this.datastore.find(MongoMatrixPlayer.class)
                .filter("_id", new ObjectId(hexId)).first();
    }

    public boolean isRegistered(UUID uniqueId) {
        return getPlayer(uniqueId) != null;
    }

    public boolean isRegistered(String name) {
        return getPlayer(name) != null;
    }

    public boolean isRegisteredById(String hexId) {
        if (hexId ==null){
            return false;
        }
        return getPlayerById(hexId) != null;
    }

    public void purgeForAllPlayers(String field) {
        this.datastore.createUpdateOperations(MongoMatrixPlayer.class).unset(field);
    }

    public void save(MatrixPlayer mongoMatrixPlayer) {
        if (mongoMatrixPlayer.getName().equals("Beelzebu")) {
            datastore.delete(getPlayer(mongoMatrixPlayer.getName()));
        }
        this.datastore.save(mongoMatrixPlayer);
    }

    public void _delete(MongoMatrixPlayer mongoMatrixPlayer) {
        this.datastore.delete(mongoMatrixPlayer);
    }

    public void addFailedLogin(UUID uniqueId, String server, String message) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_LOGIN.getQuery())) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.setString(2, trimServerName(server));
            preparedStatement.setString(3, message.length() > 1000 ? message.substring(0, 999) : message);
            preparedStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public void incrStatById(String hexId, String groupName, Statistic stat, long value) {
        if (value == 0) {
            return;
        }
        try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
            setDefaultStatsParams(callableStatement, hexId, groupName);
            setStatParam(callableStatement, stat, value);
            callableStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public void incrStatsById(String hexId, String groupName, Map<Statistic, Long> stats) {
        if (stats.isEmpty()) {
            return;
        }
        try (Connection c = dataSource.getConnection(); CallableStatement callableStatement = c.prepareCall(SQLQuery.INSERT_STATS.getQuery())) {
            setDefaultStatsParams(callableStatement, hexId, groupName);
            for (Map.Entry<Statistic, Long> ent : stats.entrySet()) {
                Statistic stat = ent.getKey();
                long value = ent.getValue();
                setStatParam(callableStatement, stat, value);
            }
            callableStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public void insertCommandLogEntryById(String hexId, String server, String command) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_COMMAND_LOG.getQuery())) {
            preparedStatement.setString(1, hexId);
            preparedStatement.setString(2, server);
            preparedStatement.setString(3, command);
            preparedStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public long getStatById(String hexId, String groupName, Statistic statistic) {
        try (Connection c = dataSource.getConnection()) {
            PreparedStatement preparedStatement = null;
            switch (statistic) {
                case KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_TOTAL.getQuery());
                    break;
                case MOB_KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_TOTAL.getQuery());
                    break;
                case DEATHS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_TOTAL.getQuery());
                    break;
                case BLOCKS_BROKEN:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_TOTAL.getQuery());
                    break;
                case BLOCKS_PLACED:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_TOTAL.getQuery());
                    break;
            }
            if (preparedStatement != null) {
                preparedStatement.setString(1, hexId);
                preparedStatement.setString(2, trimServerName(groupName));
                ResultSet res = preparedStatement.executeQuery();
                if (res.next()) {
                    return res.getLong(1);
                }
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return 0L;
    }

    public long getStatWeeklyById(String hexId, String groupName, Statistic statistic) {
        try (Connection c = dataSource.getConnection()) {
            PreparedStatement preparedStatement = null;
            switch (statistic) {
                case KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_WEEKLY.getQuery());
                    break;
                case MOB_KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_WEEKLY.getQuery());
                    break;
                case DEATHS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_WEEKLY.getQuery());
                    break;
                case BLOCKS_BROKEN:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_WEEKLY.getQuery());
                    break;
                case BLOCKS_PLACED:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_WEEKLY.getQuery());
                    break;
            }
            if (preparedStatement != null) {
                preparedStatement.setString(1, hexId);
                preparedStatement.setString(2, trimServerName(groupName));
                ResultSet res = preparedStatement.executeQuery();
                if (res.next()) {
                    return res.getLong(1);
                }
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return 0L;
    }

    public long getStatMonthlyById(String hexId, String groupName, Statistic statistic) {
        try (Connection c = dataSource.getConnection()) {
            PreparedStatement preparedStatement = null;
            switch (statistic) {
                case KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_MONTHLY.getQuery());
                    break;
                case MOB_KILLS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_MONTHLY.getQuery());
                    break;
                case DEATHS:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_MONTHLY.getQuery());
                    break;
                case BLOCKS_BROKEN:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_MONTHLY.getQuery());
                    break;
                case BLOCKS_PLACED:
                    preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_MONTHLY.getQuery());
                    break;
            }
            if (preparedStatement != null) {
                preparedStatement.setString(1, hexId);
                preparedStatement.setString(2, trimServerName(groupName));
                ResultSet res = preparedStatement.executeQuery();
                if (res.next()) {
                    return res.getLong(1);
                }
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return 0L;
    }

    public TopEntry[] getTopStatTotal(String groupName, Statistic statistic) {
        return statsTotalCache.get(statistic, stat -> {
            TopEntry[] topEntries = new TopEntry[TOP_SIZE];
            try (Connection c = dataSource.getConnection()) {
                PreparedStatement preparedStatement = null;
                switch (stat) {
                    case KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_TOP_TOTAL.getQuery());
                        break;
                    case MOB_KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_TOP_TOTAL.getQuery());
                        break;
                    case DEATHS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_TOP_TOTAL.getQuery());
                        break;
                    case BLOCKS_BROKEN:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_TOP_TOTAL.getQuery());
                        break;
                    case BLOCKS_PLACED:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_TOP_TOTAL.getQuery());
                        break;
                }
                fillTopEntries(groupName, topEntries, preparedStatement);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            return topEntries;
        });
    }


    public TopEntry[] getTopStatWeekly(String groupName, Statistic statistic) {
        return statsWeeklyCache.get(statistic, stat -> {
            TopEntry[] topEntries = new TopEntry[TOP_SIZE];
            try (Connection c = dataSource.getConnection()) {
                PreparedStatement preparedStatement = null;
                switch (stat) {
                    case KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_TOP_WEEKLY.getQuery());
                        break;
                    case MOB_KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_TOP_WEEKLY.getQuery());
                        break;
                    case DEATHS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_TOP_WEEKLY.getQuery());
                        break;
                    case BLOCKS_BROKEN:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_TOP_WEEKLY.getQuery());
                        break;
                    case BLOCKS_PLACED:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_TOP_WEEKLY.getQuery());
                        break;
                }
                fillTopEntries(groupName, topEntries, preparedStatement);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            return topEntries;
        });
    }

    public TopEntry[] getTopStatMonthly(String groupName, Statistic statistic) {
        return statsMonthlyCache.get(statistic, stat -> {
            TopEntry[] topEntries = new TopEntry[TOP_SIZE];
            try (Connection c = dataSource.getConnection()) {
                PreparedStatement preparedStatement = null;
                switch (stat) {
                    case KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_KILLS_TOP_MONTHLY.getQuery());
                        break;
                    case MOB_KILLS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_MOB_KILLS_TOP_MONTHLY.getQuery());
                        break;
                    case DEATHS:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_DEATHS_TOP_MONTHLY.getQuery());
                        break;
                    case BLOCKS_BROKEN:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_BROKEN_TOP_MONTHLY.getQuery());
                        break;
                    case BLOCKS_PLACED:
                        preparedStatement = c.prepareStatement(SQLQuery.SELECT_BLOCKS_PLACED_TOP_MONTHLY.getQuery());
                        break;
                }
                fillTopEntries(groupName, topEntries, preparedStatement);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            return topEntries;
        });
    }

    public void insertPlayStatsById(String hexId, String groupName, long playTime) {
        if (playTime == 0) {
            return;
        }

        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareCall(SQLQuery.INSERT_PLAY_STATS.getQuery())) {
            preparedStatement.setString(1, hexId);
            preparedStatement.setString(2, groupName);
            preparedStatement.setLong(3, playTime);
            preparedStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

    }

    public PlayStats getPlayStatsById(String hexId, String groupName) {
        PlayStats playStats = null;
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareCall(SQLQuery.SELECT_PLAY_STATS.getQuery())) {
            preparedStatement.setString(1, hexId);
            preparedStatement.setString(2, groupName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long joins = resultSet.getLong(1);
                    long totalPlayTime = resultSet.getLong(1);
                    playStats = new PlayStats(joins, totalPlayTime, groupName);
                }
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return playStats;
    }

    private void fillTopEntries(String groupName, TopEntry[] topEntries, PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.setString(1, trimServerName(groupName));
            ResultSet res = preparedStatement.executeQuery();
            for (int i = 0; i < TOP_SIZE; i++) {
                if (res.next()) {
                    String id = res.getString("id");
                    MatrixPlayer matrixPlayer = getPlayer(id);
                    if (matrixPlayer == null) {
                        Matrix.getLogger().warn("Player with id '" + id + "' can not be found on database.");
                        continue;
                    }
                    topEntries[i] = new TopEntry(id, matrixPlayer.getName(), res.getInt(1), i);
                    continue;
                }
                break;
            }
        }
    }

    private void setDefaultStatsParams(CallableStatement callableStatement, String hexId, String groupName) throws SQLException {
        callableStatement.setString(1, hexId);
        callableStatement.setString(2, trimServerName(groupName));
        callableStatement.setLong(3, 0);
        callableStatement.setLong(4, 0);
        callableStatement.setLong(5, 0);
        callableStatement.setLong(6, 0);
        callableStatement.setLong(7, 0);
    }

    private void setStatParam(CallableStatement callableStatement, Statistic stat, long value) throws SQLException {
        callableStatement.setLong(stat.getId(), value);
    }

    private String trimServerName(String server) {
        server = server.toLowerCase().trim();
        if (server.length() > 35) {
            server = server.substring(0, 34);
        }
        return server;
    }
}
