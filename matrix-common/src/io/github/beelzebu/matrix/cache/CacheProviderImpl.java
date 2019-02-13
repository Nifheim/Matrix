package io.github.beelzebu.matrix.cache;

import com.google.gson.JsonParseException;
import io.github.beelzebu.coins.api.CoinsAPI;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class CacheProviderImpl implements CacheProvider {

    public static final String UUID_KEY_PREFIX = "uuid:";
    public static final String NAME_KEY_PREFIX = "name:";
    public static final String USER_KEY_PREFIX = "user:";
    private final MatrixAPI api = Matrix.getAPI();

    @Override
    public Optional<UUID> getUniqueId(String name) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.exists(UUID_KEY_PREFIX + name) ? UUID.fromString(jedis.get(UUID_KEY_PREFIX + name)) : null);
        } catch (JedisException | JsonParseException ex) {
            api.debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getName(UUID uniqueId) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + uniqueId));
        } catch (JedisException | JsonParseException ex) {
            api.debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public void update(String name, UUID uniqueId) {
        if (name == null || uniqueId == null) {
            return;
        }

        String uuidStoreKey = UUID_KEY_PREFIX + name;
        String nameStoreKey = NAME_KEY_PREFIX + uniqueId;

        UUID oldUniqueId = null;
        String oldName = null;

        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            if (jedis.exists(uuidStoreKey)) { // check for old uuid to update
                oldUniqueId = UUID.fromString(jedis.get(uuidStoreKey));
                if (oldUniqueId != uniqueId) { // check if old and new are the same
                    // this is caused when player changed from cracked to premium
                    jedis.del(NAME_KEY_PREFIX + oldUniqueId);
                    jedis.set(uuidStoreKey, uniqueId.toString());
                    CoinsAPI.createPlayer(name, uniqueId);
                    CoinsAPI.setCoins(uniqueId, CoinsAPI.getCoins(oldUniqueId));
                    CoinsAPI.resetCoins(oldUniqueId);
                }
            } else { // store uuid because it doesn't exists.
                jedis.set(uuidStoreKey, uniqueId.toString());
            }
            if (jedis.exists(nameStoreKey)) { // check for old name to update
                oldName = jedis.get(nameStoreKey);
                if (!Objects.equals(oldName, name)) { // check if old and new are the same
                    jedis.del(UUID_KEY_PREFIX + oldName);
                    jedis.set(nameStoreKey, name);
                }
            } else { // store name because it doesn't exists.
                jedis.set(nameStoreKey, name);
            }
        }

        if (Objects.equals(name, oldName == null ? name : oldName) && Objects.equals(uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId)) {
            return;
        }
        new NameUpdatedMessage(name, oldName == null ? name : oldName, uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId).send();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(UUID uniqueId) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            try {
                if (jedis.exists(USER_KEY_PREFIX + uniqueId)) {
                    return Optional.ofNullable(MongoMatrixPlayer.fromHash(jedis.hgetAll(USER_KEY_PREFIX + uniqueId)));
                }
            } catch (ClassCastException e) {
                return getPlayer(uniqueId);
            } catch (NullPointerException e) {
                api.debug(e);
                api.debug(Matrix.GSON.toJson(jedis.hgetAll(USER_KEY_PREFIX + uniqueId)));
                jedis.del(USER_KEY_PREFIX + uniqueId);
            }
        } catch (JedisDataException e) {
            return getPlayer(uniqueId);
        } catch (JedisException | JsonParseException e) {
            api.debug(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(String name) {
        UUID uniqueId = getUniqueId(name).orElse(api.getPlugin().getUniqueId(name));
        return uniqueId != null ? getPlayer(uniqueId) : Optional.empty();
    }

    @Override
    public Set<MatrixPlayer> getPlayers() {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            return jedis.keys("user:*").stream().map(cached -> getPlayer(UUID.fromString(cached.split(":")[1])).orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (JedisException ex) {
            api.debug(ex);
        }
        return Collections.emptySet();
    }

    @Override
    public void removePlayer(MatrixPlayer player) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            getPlayer(player.getUniqueId()).orElse(player).save(); // save cached version to database
            jedis.del(USER_KEY_PREFIX + player.getUniqueId()); // remove it from redis
        } catch (JedisException | JsonParseException ex) {
            api.debug(ex);
        }
    }
}
