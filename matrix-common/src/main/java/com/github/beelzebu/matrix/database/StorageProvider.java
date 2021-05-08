package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.database.sql.SQLQuery;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.converters.UUIDConverter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageProvider {

    private HikariDataSource dataSource;
    private final Datastore datastore;

    public StorageProvider(@NotNull MatrixAPIImpl api) {
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

    public @Nullable MatrixPlayer getPlayer(UUID uniqueId) {
        return this.datastore.find(MongoMatrixPlayer.class)
                .filter("uniqueId", uniqueId).first();
    }

    public MatrixPlayer getPlayerByName(@NotNull String name) {
        return Optional.ofNullable(
                this.datastore.find(MongoMatrixPlayer.class)
                        .filter("lowercaseName", name.toLowerCase()).first())
                .orElse(
                        this.datastore.find(MongoMatrixPlayer.class)
                                .filter("name", name).first());
    }

    public @Nullable MatrixPlayer getPlayerById(@NotNull String hexId) {
        return this.datastore.find(MongoMatrixPlayer.class)
                .filter("_id", new ObjectId(hexId)).first();
    }

    public boolean isRegistered(UUID uniqueId) {
        return getPlayer(uniqueId) != null;
    }

    public boolean isRegisteredByName(@NotNull String name) {
        return getPlayerByName(name) != null;
    }

    public boolean isRegisteredById(@Nullable String hexId) {
        if (hexId == null) {
            return false;
        }
        return getPlayerById(hexId) != null;
    }

    public void purgeForAllPlayers(String field) {
        this.datastore.createUpdateOperations(MongoMatrixPlayer.class).unset(field);
    }

    public <T extends MatrixPlayer> T save(T matrixPlayer) {
        this.datastore.save(matrixPlayer);
        return matrixPlayer;
    }

    public void addFailedLogin(@NotNull UUID uniqueId, String server, @NotNull String message) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_LOGIN.getQuery())) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.setString(2, trimServerName(server));
            preparedStatement.setString(3, message.length() > 1000 ? message.substring(0, 999) : message);
            preparedStatement.executeUpdate();
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

    public @Nullable PlayStats getPlayStatsById(String hexId, String groupName) {
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

    private String trimServerName(String server) {
        server = server.toLowerCase().trim();
        if (server.length() > 35) {
            server = server.substring(0, 34);
        }
        return server;
    }
}
