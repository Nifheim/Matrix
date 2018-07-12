package io.github.beelzebu.matrix.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public abstract class MatrixPlayer {

    protected final MatrixAPI core = MatrixAPI.getInstance();
    /**
     * Obtiene la UUID de esta instancia de MatrixPlayer
     *
     * @return UUID del usuario.
     */
    @Getter
    protected final UUID uniqueId;
    /**
     * Obtiene los nombres de todos los jugadores ignorados por este usuario.
     *
     * @return todos los jugadores ignorados.
     */
    @Getter
    private final HashSet<String> ignoredPlayers = new HashSet<>();
    /**
     * Obtiene si el usuario ha iniciado sesión en el auth de seguridad.
     *
     * @return si el usuario inició sesión o no.
     * @param authed establece la sesión del usuario
     */
    @Getter
    @Setter
    public boolean authed = false;
    /**
     * Obtiene si el usuario puede ver comandos de otros jugadores en el
     * servidor.
     *
     * @return <i>true</i> en caso de que pueda hacerlo o <i>false</i> de otra
     * forma.
     */
    @Getter
    private boolean watcher;
    private String chatcolor = "";

    public MatrixPlayer(UUID uuid) {
        uniqueId = uuid;
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            try {
                setWatcher(core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class).get("watcher").getAsBoolean());
                JsonArray ignored = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class).getAsJsonArray("ignored");
                ignored.forEach(ip -> ignoredPlayers.add(ip.getAsString()));
            } catch (NullPointerException | JsonSyntaxException | JedisException ignore) {
                core.log("Exception loading data from db for: " + uniqueId);
            }
        }
    }

    /**
     * Obtiene el nombre de este usuario desde la base de datos si está offline.
     *
     * @return nombre del usuario.
     */
    public abstract String getName();

    /**
     * Obtiene si el usuario tiene la opción especificada habilitada o no.
     *
     * @param option opción para revisar
     * @return -
     */
    public abstract boolean getOption(PlayerOptionType option);

    /**
     * Establece el estado de una opción para el usuario.
     *
     * @param option opción para establecer.
     * @param status estado de la opción.
     */
    public abstract void setOption(PlayerOptionType option, boolean status);

    /**
     * Establece todas las opciones para el usuario con los valores
     * especificados.
     *
     * @param options valores para establecer.
     */
    public abstract void setAllOptions(Map<PlayerOptionType, Boolean> options);

    /**
     * Obtiene todas las opciones activas del usuario.
     *
     * @return todas las opciones activas.
     */
    public abstract Set<PlayerOptionType> getActiveOptions();

    /**
     * Obtiene si el usuario está en un lobby.
     *
     * @return si el usuario está en lobby o no.
     */
    public abstract boolean isInLobby();

    /**
     * Establece el nick del usuario, en caso de que contenga espacios también
     * se establecerá el prefijo de este.
     *
     * @param nick nuevo nick a establecer.
     */
    public abstract void setNick(String nick);

    /**
     * Obtiene la dirección IP desde la cual se está conectando este usuario.
     *
     * @return direcció IP real del usuario.
     */
    public abstract String getIP();

    /**
     * Revisa si el usuario tiene un permiso asignado o heredado de otro rango.
     *
     * @param permission permiso para revisar.
     * @return <i>true</i> en caso de que tenga el permiso o <i>false</i> de
     * otra forma.
     */
    public abstract boolean hasPermission(String permission);

    /**
     * Obtiene el displayname del usuario con prefix y colores.
     *
     * @return displayname del usuario.
     */
    public final String getNickname() {
        return core.getDisplayName(uniqueId, false);
    }

    /**
     * Obtiene el nivel del usuario.
     *
     * @return nivel del usuario desde la base de datos.
     */
    public final int getLevel() {
        return NetworkXP.getLevelForPlayer(uniqueId);
    }

    /**
     * Obtiene la xp total del usuario.
     *
     * @return xp del usuario desde la base de datos.
     */
    public final long getXP() {
        return NetworkXP.getXPForPlayer(uniqueId);
    }

    /**
     * Establece si el usuario puede ver los comandos de los demás usuarios.
     *
     * @param watcher -
     */
    public final void setWatcher(boolean watcher) {
        core.getRedis().setData(uniqueId, "watcher", watcher);
        this.watcher = watcher;
    }

    /**
     * Añade a un usuario a la lista de ignorados.
     *
     * @param name usuario para ignorar.
     */
    public final void addIgnored(String name) {
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            JsonObject userdata = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class);
            JsonArray ignored = userdata.getAsJsonArray("ignored");
            if (ignored == null) {
                ignored = new JsonArray();
            }
            ignored.add(name);
            userdata.add("ignored", ignored);
            jedis.hset("ncore_data", uniqueId.toString(), userdata.toString());
            ignoredPlayers.add(name);
        }
    }

    /**
     * Elimina un usuario de la lista de ignorados de este jugador.
     *
     * @param name usuario a eliminar.
     */
    public final void removeIgnored(String name) {
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            JsonObject userdata = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class);
            JsonArray ignored = userdata.getAsJsonArray("ignored");
            if (ignored == null) {
                ignored = new JsonArray();
            }
            Iterator<JsonElement> it = ignored.iterator();
            while (it.hasNext()) {
                if (it.next().getAsString().equalsIgnoreCase(name)) {
                    it.remove();
                }
            }
            userdata.add("ignored", ignored);
            jedis.hset("ncore_data", uniqueId.toString(), userdata.toString());
            ignoredPlayers.remove(name);
        }
    }

    /**
     * Obtiene el color de chat que debería tener el usuario.
     *
     * @return color de chat del usuario.
     */
    public final String getChatColor() {
        if (chatcolor != null) {
            return chatcolor;
        }
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            return chatcolor = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class).get("chatcolor").getAsString();
        } catch (JedisException | NullPointerException ex) {
            return "";
        }
    }

    /**
     * Establece el color de chat del usuario
     *
     * @param color
     */
    public final void setChatColor(ChatColor color) {
        chatcolor = color.toString().replace("§", "");
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            JsonObject userdata = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class);
            userdata.addProperty("chatcolor", chatcolor);
            jedis.hset("ncore_data", uniqueId.toString(), userdata.toString());
        }
    }
}
