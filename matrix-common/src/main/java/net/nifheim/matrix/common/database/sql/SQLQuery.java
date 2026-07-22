package net.nifheim.matrix.common.database.sql;

/**
 * @author Jaime Suárez
 */
public enum SQLQuery {
    INSERT_FAILED_LOGIN("INSERT INTO failed_login(uniqueId, server, message) VALUES(?, ?, ?)"),
    INSERT_COMMAND_LOG("INSERT INTO command_log(user_id, server, command) VALUES (?, ?, ?)"),
    INSERT_PLAYER("INSERT INTO player(uniqueId, name, locale) VALUES (?, ?, ?)"),
    SELECT_PLAYER_BY_UUID("SELECT id, uniqueId, discordId, name, display_name, premium, registered, locale, registered_at, created_at, updated_at, (SELECT timestamp FROM handshake h WHERE h.player_id = player.id ORDER BY h.timestamp DESC LIMIT 1) AS last_login FROM player WHERE uniqueId = ?"),
    SELECT_PLAYER_BY_NAME("SELECT id, uniqueId, discordId, name, display_name, premium, registered, locale, registered_at, created_at, updated_at, (SELECT timestamp FROM handshake h WHERE h.player_id = player.id ORDER BY h.timestamp DESC LIMIT 1) AS last_login FROM player WHERE name = ?"),
    INSERT_PLAYER_HANDSHAKE("INSERT INTO handshake(ip, protocol, version, hostname) VALUES (?, ?, ?, ?)"),
    INSERT_PLAYER_LOGIN_STATE("INSERT INTO player_login(handshake_id, state) VALUES (?, ?)"),
    LINK_HANDSHAKE("UPDATE handshake SET player_id = ? WHERE id = ?"),
    UPDATE_PLAYER("UPDATE player SET uniqueId = ?, discordId = ?, name = ?, display_name = ?, premium = ?, registered = ?, locale = ?, registered_at = ? WHERE id = ?");

    private final String query;

    SQLQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public enum LoginState {
        PRE_LOGIN("PRE_LOGIN"),
        LOGIN("LOGIN"),
        POST_LOGIN("POST_LOGIN"),
        DISCONNECT("DISCONNECT");

        private final String state;

        LoginState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }
}
