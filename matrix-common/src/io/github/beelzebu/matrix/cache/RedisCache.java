package io.github.beelzebu.matrix.cache;

import com.google.gson.JsonParseException;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisCache implements CacheProvider {

    @Override
    public Optional<UUID> getUniqueId(String name) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.get("uuid:" + name) != null ? UUID.fromString(jedis.get("uuid:" + name)) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getName(UUID uniqueId) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.get("name:" + uniqueId));
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(UUID uniqueId) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.get("user:" + uniqueId) != null ? Matrix.getAPI().getGson().fromJson(jedis.get("user:" + uniqueId), MongoMatrixPlayer.class) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(String name) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            Optional<UUID> uuid = getUniqueId(name);
            return Optional.ofNullable(uuid.map(uuid1 -> Matrix.getAPI().getGson().fromJson(jedis.get("user:" + uuid1), MongoMatrixPlayer.class)).orElse(null));
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Set<MatrixPlayer> getPlayers() {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            Set<String> cached = jedis.keys("user:*");
            return cached.stream().map(cachedu -> getPlayer(UUID.fromString(cachedu.split(":")[1])).orElse(null)).collect(Collectors.toSet());
        } catch (JedisException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Collections.emptySet();
    }
}
