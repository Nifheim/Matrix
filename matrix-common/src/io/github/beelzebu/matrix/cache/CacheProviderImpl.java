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
public class CacheProviderImpl implements CacheProvider {

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
    public void update(String name, UUID uniqueId) {
        if (name == null || uniqueId == null) {
            return;
        }
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            jedis.set("uuid:" + name, uniqueId.toString());
            jedis.set("name:" + uniqueId.toString(), name);
        }
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(UUID uniqueId) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            return Optional.ofNullable(jedis.exists("user:" + uniqueId) ? MongoMatrixPlayer.fromHash(jedis.hgetAll("user:" + uniqueId)) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(String name) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            UUID uniqueId = getUniqueId(name).orElse(Matrix.getAPI().getPlugin().getUniqueId(name));
            return Optional.ofNullable(uniqueId != null ? MongoMatrixPlayer.fromHash(jedis.hgetAll("user:" + uniqueId)) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Set<MatrixPlayer> getPlayers() {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            return jedis.keys("user:*").stream().map(cached -> getPlayer(UUID.fromString(cached.split(":")[1])).orElse(null)).collect(Collectors.toSet());
        } catch (JedisException ex) {
            Matrix.getAPI().debug(ex);
        }
        return Collections.emptySet();
    }

    @Override
    public void removePlayer(MatrixPlayer player) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            getPlayer(player.getUniqueId()).orElse(player).save();
            jedis.del("user:" + player.getUniqueId());
        } catch (JedisException | JsonParseException ex) {
            Matrix.getAPI().debug(ex);
        }
    }
}
