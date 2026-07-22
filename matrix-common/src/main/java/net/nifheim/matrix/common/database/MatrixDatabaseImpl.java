package net.nifheim.matrix.common.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import net.nifheim.matrix.api.database.MatrixDatabase;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.service.InactiveServiceException;
import net.nifheim.matrix.common.config.MatrixConfiguration;
import net.nifheim.matrix.common.database.sql.SQLQuery;
import net.nifheim.matrix.common.player.MatrixPlayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;


/**
 * The MatrixDatabaseImpl class is an implementation of the MatrixDatabase interface.
 * It provides methods for retrieving, storing, and updating player data in a database.
 *
 * @author Jaime Suárez
 */
public class MatrixDatabaseImpl implements MatrixDatabase {

    private final Logger logger;
    private HikariDataSource dataSource;
    private final ConcurrentMap<Long, Lock> locks = new ConcurrentHashMap<>();

    // SQL prepared statements parameter setters
    private static final Map<Class<?>, PreparedStatementSetter> TYPE_SETTERS = new HashMap<>();

    static {
        TYPE_SETTERS.put(String.class, (ps, entry) -> ps.setString(entry.getKey(), (String) entry.getValue()));
        TYPE_SETTERS.put(Integer.class, (ps, entry) -> ps.setInt(entry.getKey(), (Integer) entry.getValue()));
        TYPE_SETTERS.put(Double.class, (ps, entry) -> ps.setDouble(entry.getKey(), (Double) entry.getValue()));
        TYPE_SETTERS.put(Long.class, (ps, entry) -> ps.setLong(entry.getKey(), (Long) entry.getValue()));
        TYPE_SETTERS.put(Boolean.class, (ps, entry) -> ps.setBoolean(entry.getKey(), (Boolean) entry.getValue()));
        TYPE_SETTERS.put(UUID.class, (ps, entry) -> ps.setString(entry.getKey(), entry.getValue().toString()));
        TYPE_SETTERS.put(Locale.class, (ps, entry) -> ps.setString(entry.getKey(), entry.getValue().toString()));
        TYPE_SETTERS.put(Date.class, (ps, entry) -> ps.setDate(entry.getKey(), new java.sql.Date(((Date) entry.getValue()).getTime())));
    }

    public MatrixDatabaseImpl(@NotNull MatrixConfiguration config, Logger logger) {
        this.logger = logger;
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("Matrix MySQL Connection Pool");
        hc.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        hc.addDataSourceProperty("serverName", config.getMariaDbConfig().getHost());
        hc.addDataSourceProperty("port", config.getMariaDbConfig().getPort());
        hc.addDataSourceProperty("databaseName", config.getMariaDbConfig().getDatabase());
        Map<String, String> properties = new HashMap<>();
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf8");
        properties.put("useSSL", "false");
        properties.put("verifyServerCertificate", "false");
        properties.put("autoReconnect", "true");
        properties.put("useMysqlMetadata", "false");
        String propertiesString = properties.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";"));
        hc.addDataSourceProperty("properties", propertiesString);
        hc.setUsername(config.getMariaDbConfig().getUsername());
        hc.setPassword(config.getMariaDbConfig().getPassword());
        hc.setMaxLifetime(60000L);
        hc.setMinimumIdle(1);
        hc.setIdleTimeout(30000L);
        hc.setConnectionTimeout(10000L);
        hc.setMaximumPoolSize(config.getMariaDbConfig().getPoolSize());
        hc.validate();
        try {
            this.dataSource = new HikariDataSource(hc);
        } catch (Exception ex) {
            logger.error("An exception has occurred while starting connection pool", ex);
        }
    }

    @Override
    public @Nullable MatrixPlayer getPlayer(@NotNull UUID uniqueId) {
        Objects.requireNonNull(uniqueId);
        Lock lock = locks.get(uniqueId);
        if (lock != null) {
            lock.lock();
        }
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.SELECT_PLAYER_BY_UUID, uniqueId); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return new MatrixPlayerImpl(resultSet);
            }

        } catch (SQLException ex) {
            logger.error("An exception has occurred while retrieving player", ex);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public MatrixPlayer createPlayer(UUID uniqueId, String name, Locale locale) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.INSERT_PLAYER, uniqueId, name, locale)) {
                preparedStatement.executeUpdate();
            }
            try (PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.SELECT_PLAYER_BY_UUID, uniqueId); ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new MatrixPlayerImpl(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("An error has occurred while creating player", ex);
        }
        throw new RuntimeException("Player not found");
    }

    public @Nullable MatrixPlayer getPlayerByName(@NotNull String name) {
        if (name.isBlank() || name.length() > 16 || name.length() < 2) {
            throw new IllegalArgumentException("name must be valid");
        }
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.SELECT_PLAYER_BY_NAME, name); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return new MatrixPlayerImpl(resultSet);
            }
        } catch (SQLException ex) {
            logger.error("An exception has occurred while retrieving player", ex);
        }
        return null;
    }

    @Override
    public boolean isStored(UUID uniqueId) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.SELECT_PLAYER_BY_UUID, uniqueId); ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next();
        } catch (SQLException ex) {
            throw new RuntimeException("An error has occurred while retrieving player", ex);
        }
    }

    @NotNull
    public MatrixPlayer save(@NotNull MatrixPlayer player) {
        Objects.requireNonNull(player.getUniqueId());
        Objects.requireNonNull(player.getName());
        Lock lock = locks.computeIfAbsent(player.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            logger.info("Saving player {} ({} - {})", player.getName(), player.getId(), player.getUniqueId());
            try (Connection c = dataSource.getConnection()) {
                // update the player, then select the updated player from the database
                try (PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.UPDATE_PLAYER, player.getUniqueId(), player.getDiscordId(), player.getName(), player.getDisplayName(), player.isPremium(), player.isRegistered(), player.getLocale(), player.getRegistration(), player.getId())) {
                    preparedStatement.executeUpdate();
                }
                try (PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.SELECT_PLAYER_BY_UUID, player.getUniqueId()); ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new MatrixPlayerImpl(resultSet);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("An exception has occurred while saving player", ex);
        } finally {
            lock.unlock();
            locks.remove(player.getId());
        }

        throw new RuntimeException("Player not found");
    }

    @Override
    public void shutdown() throws InactiveServiceException {
        if (!locks.isEmpty()) {
            locks.values().forEach(Lock::lock);
            locks.clear();
        }
        dataSource.close();
    }

    @Override
    public boolean isActive() {
        return dataSource != null && dataSource.isRunning() && !dataSource.isClosed();
    }

    @Override
    public long saveHandshakeRequest(InetAddress address, int protocol, String version, @Nullable String hostname) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.INSERT_PLAYER_HANDSHAKE, address.getHostAddress(), protocol, version, hostname)) {
                preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("An error has occurred while storing handshake request", ex);
        }
        throw new RuntimeException("An error has occurred while storing handshake request");
    }

    public void saveLoginState(long handshakeId, SQLQuery.LoginState loginState) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.INSERT_PLAYER_LOGIN_STATE, handshakeId, loginState.getState())) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("An error has occurred while storing login state", ex);
        }
    }

    public void linkHandshake(@NotNull MatrixPlayer player, long handshakeId) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = prepareStatement(c, SQLQuery.LINK_HANDSHAKE, player.getId(), handshakeId)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("An error has occurred while linking handshake", ex);
        }
    }

    @FunctionalInterface
    public interface PreparedStatementSetter {

        void apply(PreparedStatement ps, Map.Entry<Integer, Object> entry) throws SQLException;
    }

    public Connection getSQLConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public PreparedStatement prepareStatement(Connection connection, String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        int i = 1;
        for (Object param : params) {
            final int index = i;
            if (param == null) {
                preparedStatement.setObject(index, null);
            } else {
                TYPE_SETTERS.get(param.getClass()).apply(preparedStatement, Map.entry(index, param));
            }
            i++;
        }
        return preparedStatement;
    }

    public PreparedStatement prepareStatement(Connection connection, SQLQuery query, Object... params) throws SQLException {
        return prepareStatement(connection, query.getQuery(), params);
    }
}
