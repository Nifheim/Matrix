package net.nifheim.matrix.auth.paper.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.auth.paper.exception.UserNotRegisteredException;
import net.nifheim.matrix.auth.paper.security.HashedPassword;
import net.nifheim.matrix.auth.paper.security.PasswordEncryption;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import org.slf4j.Logger;

public class AuthDatabase {

    private static final String QUERY_GET_PASSWORD_HASH = "SELECT password_hash, password_salt FROM player_identity pi JOIN matrix.player p ON p.id = pi.player_id WHERE p.uniqueId = ?;";
    private static final String QUERY_INSERT_PASSWORD_HASH = "INSERT INTO player_identity(player_id, password_hash, password_salt, rounds, algorithm) VALUES ((SELECT id FROM player WHERE uniqueId = ?), ?, ?, ?, ?);";
    private static final String QUERY_INSERT_PASSWORD_HASH_BY_NAME = "INSERT INTO player_identity(player_id, password_hash, password_salt, rounds, algorithm) VALUES ((SELECT id FROM player WHERE name like ?), ?, ?, ?, ?);";
    private static final String QUERY_UPDATE_PASSWORD_HASH = "UPDATE player_identity SET password_hash = ?, password_salt = ?, rounds = ? WHERE player_id = (SELECT id FROM player WHERE uniqueId = ?);";

    private final MatrixDatabaseImpl database = (MatrixDatabaseImpl) MatrixProvider.getAPI().getDatabase();
    private final Logger logger;

    public AuthDatabase(Logger logger) {
        this.logger = logger;
    }

    public HashedPassword getPasswordHash(UUID uniqueId) throws UserNotRegisteredException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, QUERY_GET_PASSWORD_HASH, uniqueId);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return new HashedPassword(PasswordEncryption.ROUNDS, resultSet.getString("password_hash"), resultSet.getString("password_salt"));
            }
            throw new UserNotRegisteredException("No result found for given UUID");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertHashedPassword(UUID uniqueId, HashedPassword hashedPassword) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, QUERY_INSERT_PASSWORD_HASH, uniqueId, hashedPassword.hash(), hashedPassword.salt(), hashedPassword.iterations(), "pbkdf2_sha256")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertHashedPassword(String name, HashedPassword hashedPassword) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, QUERY_INSERT_PASSWORD_HASH_BY_NAME, name, hashedPassword.hash(), hashedPassword.salt(), hashedPassword.iterations(), "pbkdf2_sha256")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateHashedPassword(UUID uniqueId, HashedPassword hashedPassword) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, QUERY_UPDATE_PASSWORD_HASH, hashedPassword.hash(), hashedPassword.salt(), hashedPassword.iterations(), uniqueId)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return database.getSQLConnection();
    }

    private PreparedStatement prepareStatement(Connection connection, String query, Object... params) throws SQLException {
        return database.prepareStatement(connection, query, params);
    }
}
