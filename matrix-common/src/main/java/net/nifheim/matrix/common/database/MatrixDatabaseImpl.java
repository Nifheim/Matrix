package net.nifheim.matrix.common.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.internal.MongoClientImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.FindOptions;
import dev.morphia.query.filters.Filters;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
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
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.types.ObjectId;
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

    private static final FindOptions SINGLE_RESULT = new FindOptions().limit(1);
    private final MatrixConfiguration config;
    private final Logger logger;
    private HikariDataSource dataSource;
    private final Datastore datastore;
    private final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<>();

    // SQL prepared statements parameter setters
    private static final Map<Class<?>, PreparedStatementSetter> TYPE_SETTERS = new HashMap<>();

    static {
        TYPE_SETTERS.put(String.class, (ps, entry) -> ps.setString(entry.getKey(), (String) entry.getValue()));
        TYPE_SETTERS.put(Integer.class, (ps, entry) -> ps.setInt(entry.getKey(), (Integer) entry.getValue()));
        TYPE_SETTERS.put(Double.class, (ps, entry) -> ps.setDouble(entry.getKey(), (Double) entry.getValue()));
        TYPE_SETTERS.put(Long.class, (ps, entry) -> ps.setLong(entry.getKey(), (Long) entry.getValue()));
        TYPE_SETTERS.put(Boolean.class, (ps, entry) -> ps.setBoolean(entry.getKey(), (Boolean) entry.getValue()));
        TYPE_SETTERS.put(UUID.class, (ps, entry) -> ps.setString(entry.getKey(), entry.getValue().toString()));
    }

    public MatrixDatabaseImpl(@NotNull MatrixConfiguration config, Logger logger) {
        this.config = config;
        this.logger = logger;
        MongoClientSettings clientSettings = MongoClientSettings.builder().credential(MongoCredential.createCredential(config.getMongoConfig().getUsername(), config.getMongoConfig().getDatabase(), config.getMongoConfig().getPassword().toCharArray())).applyConnectionString(new ConnectionString("mongodb://%s:%s".formatted(config.getMongoConfig().getHost(), config.getMongoConfig().getPort()))).uuidRepresentation(UuidRepresentation.UNSPECIFIED).codecRegistry(CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new UuidAsStringCodec()), MongoClientSettings.getDefaultCodecRegistry())).build();
        MongoClientImpl client = new MongoClientImpl(clientSettings, null);
        datastore = Morphia.createDatastore(client, "matrix");
        datastore.getMapper().map(MongoMatrixPlayer.class);
        datastore.ensureIndexes();
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
    public @Nullable MongoMatrixPlayer getPlayer(@NotNull UUID uniqueId) {
        Objects.requireNonNull(uniqueId);
        Lock lock = locks.get(uniqueId.toString());
        if (lock != null) {
            lock.lock();
        }
        try {
            return this.datastore.find(MongoMatrixPlayer.class).filter(Filters.eq("uniqueId", uniqueId.toString())).first(SINGLE_RESULT);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    @Override
    public @Nullable MongoMatrixPlayer getPlayer(@NotNull String hexId) {
        Objects.requireNonNull(hexId);
        if (hexId.isBlank()) {
            throw new IllegalArgumentException("hexId cannot be blank");
        }
        if (!ObjectId.isValid(hexId)) {
            throw new IllegalArgumentException("hexId is not a valid ObjectId");
        }
        Lock lock = locks.get(hexId);
        if (lock != null) {
            lock.lock();
        }
        try {
            return datastore.find(MongoMatrixPlayer.class).filter(Filters.eq("_id", new ObjectId(hexId))).first(SINGLE_RESULT);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public @Nullable MongoMatrixPlayer getPlayerByName(@NotNull String name) {
        if (name == null || name.isBlank() || name.length() > 16 || name.length() < 2) {
            throw new IllegalArgumentException("name must be valid");
        }
        return datastore.find(MongoMatrixPlayer.class).filter(Filters.eq("lowercaseName", name.toLowerCase())).first(SINGLE_RESULT);
    }

    @Override
    public boolean isStored(@NotNull MatrixPlayer matrixPlayer) {
        if (matrixPlayer.getId() != null) {
            return isStored(matrixPlayer.getId());
        }
        return isStored(matrixPlayer.getUniqueId());
    }

    @Override
    public boolean isStored(UUID uniqueId) {
        try (MongoCursor<Document> cursor = datastore.getDatabase().getCollection(config.getMongoConfig().getDatabase()).find(new Document("uniqueId", uniqueId)).limit(1).cursor()) {
            return cursor.hasNext();
        }
    }

    @Override
    public boolean isStored(String hexId) {
        // TODO: if it works with the hexId without transforming it to ObjectId, then keep it
        try (MongoCursor<Document> cursor = datastore.getDatabase().getCollection(config.getMongoConfig().getDatabase()).find(new Document("_id", hexId)).limit(1).cursor()) {
            return cursor.hasNext();
        }
    }

    @NotNull
    public <T extends MatrixPlayer> T save(@NotNull T matrixPlayer) {
        Objects.requireNonNull(matrixPlayer.getUniqueId());
        Objects.requireNonNull(matrixPlayer.getName());
        Lock lock = locks.computeIfAbsent(matrixPlayer.getId(), k -> new ReentrantLock());
        boolean locked = lock.tryLock();
        if (locked) {
            try {
                logger.info("Saving player {} ({} - {})", matrixPlayer.getName(), matrixPlayer.getId(), matrixPlayer.getUniqueId());
                this.datastore.save(matrixPlayer);
                updateStats(matrixPlayer);
            } catch (Exception ex) {
                logger.error("An exception has occurred while saving player", ex);
            } finally {
                lock.unlock();
                locks.remove(matrixPlayer.getId());
            }
        }
        return matrixPlayer;
    }

    @Override
    public void shutdown() throws InactiveServiceException {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    public void storeHandshakeRequest(InetAddress address, int protocol, String version, String hostname) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement =
                prepareStatement(c, SQLQuery.INSERT_HANDSHAKE.getQuery(), address.getHostAddress(), protocol, version, hostname)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An exception has occurred while storing handshake request", ex);
        }
    }

    public void updateStats(MatrixPlayer player) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement =
                prepareStatement(c, SQLQuery.INSERT_PLAYER.getQuery(), player.getId(), player.getUniqueId(), player.getName(), player.getId(), player.getUniqueId(), player.getName())) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An exception has occurred while storing player", ex);
        }
    }

    public void saveAddress(MatrixPlayer player, InetAddress address) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_PLAYER_ADDRESS.getQuery())) {
            preparedStatement.setString(1, address.getHostAddress());
            preparedStatement.setString(2, player.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An exception has occurred while storing player address", ex);
        }
    }

    public void saveLogin(MatrixPlayer player) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_PLAYER_LOGIN.getQuery())) {
            preparedStatement.setString(1, player.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An exception has occurred while storing player login", ex);
        }
    }

    public void saveLogout(MatrixPlayer player) {
        try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(SQLQuery.INSERT_PLAYER_LOGOUT.getQuery())) {
            preparedStatement.setString(1, player.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            logger.error("An exception has occurred while storing player logout", ex);
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
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int i = 1;
        for (Object param : params) {
            final int index = i;
            TYPE_SETTERS.get(param.getClass()).apply(preparedStatement, Map.entry(index, param));
            i++;
        }
        return preparedStatement;
    }
}
