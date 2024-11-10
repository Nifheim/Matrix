-- enable scheduler
SET GLOBAL event_scheduler = ON;
-- players and sessions
DROP TABLE IF EXISTS player_logout;
DROP TABLE IF EXISTS player_login;
DROP TABLE IF EXISTS player_address;
-- drop tables, events and procedures
DROP TABLE IF EXISTS stats_total;
DROP TABLE IF EXISTS stats_monthly;
DROP TABLE IF EXISTS stats_weekly;
DROP TABLE IF EXISTS failed_login;
DROP TABLE IF EXISTS command_log;
DROP TABLE IF EXISTS play_stats;
DROP VIEW IF EXISTS play_stats_total;
DROP PROCEDURE IF EXISTS insert_stats;
DROP EVENT IF EXISTS drop_monthly_stats;
DROP EVENT IF EXISTS drop_weekly_stats;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS handshake;

-- create players and sessions tables
CREATE TABLE IF NOT EXISTS handshake
(
    id        UUID         NOT NULL PRIMARY KEY DEFAULT UUID(),
    ip        VARCHAR(15)  NOT NULL,
    protocol  INT UNSIGNED NOT NULL,
    version   VARCHAR(32)  NOT NULL,
    hostname  VARCHAR(255) NULL,
    timestamp TIMESTAMP    NOT NULL             DEFAULT NOW(),
    FULLTEXT (ip)
);

CREATE TABLE IF NOT EXISTS player
(
    id       UUID        NOT NULL PRIMARY KEY DEFAULT UUID(),
    hexId    char(24)    NOT NULL UNIQUE,
    uniqueId UUID        NOT NULL UNIQUE,
    name     VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS player_address
(
    id        UUID        NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    ip        VARCHAR(15) NOT NULL,
    timestamp TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS player_login
(
    id        UUID      NOT NULL PRIMARY KEY DEFAULT UUID(),
    player_id UUID      NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    timestamp TIMESTAMP NOT NULL             DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS player_logout
(
    id        UUID      NOT NULL PRIMARY KEY DEFAULT UUID(),
    player_id UUID      NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    timestamp TIMESTAMP NOT NULL             DEFAULT NOW()
);


-- stats
-- create stats tables
CREATE TABLE IF NOT EXISTS stats_total
(
    player_id    UUID PRIMARY KEY NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    server       VARCHAR(50)      NOT NULL,
    kills        BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    mobKills     BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    deaths       BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    blocksBroken BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    blocksPlaced BIGINT UNSIGNED  NOT NULL DEFAULT 0,
    UNIQUE KEY user_server_uq (player_id, server)
);

CREATE TABLE IF NOT EXISTS stats_monthly LIKE stats_total;

CREATE TABLE IF NOT EXISTS stats_weekly LIKE stats_total;

CREATE TABLE IF NOT EXISTS failed_login
(
    id        UUID PRIMARY KEY NOT NULL DEFAULT UUID(),
    date      DATETIME         NOT NULL DEFAULT SYSDATE(),
    player_id UUID             NOT NULL,
    name      VARCHAR(16)      NOT NULL,
    server    VARCHAR(35)      NOT NULL,
    message   TEXT                      DEFAULT NULL,
    UNIQUE KEY date_user (date, player_id)
);

CREATE TABLE IF NOT EXISTS command_log
(
    id        UUID PRIMARY KEY NOT NULL DEFAULT UUID(),
    player_id UUID             NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    server    VARCHAR(50)      NOT NULL,
    command   VARCHAR(256)     NOT NULL,
    date      DATETIME         NOT NULL DEFAULT SYSDATE()
);

CREATE TABLE IF NOT EXISTS play_stats
(
    id           UUID PRIMARY KEY NOT NULL DEFAULT UUID(),
    player_id    UUID             NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    server_group VARCHAR(20)      NOT NULL,
    play_time    LONG,
    timestamp    TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE VIEW IF NOT EXISTS play_stats_total AS
SELECT COUNT(id) joins, SUM(play_time) total_play_time, server_group, player_id
FROM play_stats
GROUP BY server_group, player_id;

CREATE OR REPLACE PROCEDURE insert_stats(v_player_id UUID, v_server VARCHAR(50), v_kills LONG, v_mobKills LONG,
                                         v_deaths LONG, v_blocksBroken LONG, v_blocksPlaced LONG)
BEGIN
    IF (SELECT COUNT(player_id) FROM stats_weekly WHERE player_id = v_player_id AND server = v_server) = 1 THEN
        UPDATE stats_weekly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE player_id = v_player_id
          AND server = v_server;
    ELSE
        INSERT INTO stats_weekly(player_id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_player_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(player_id) FROM stats_monthly WHERE player_id = v_player_id AND server = v_server) = 1 THEN
        UPDATE stats_monthly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE player_id = v_player_id
          AND server = v_server;
    ELSE
        INSERT INTO stats_monthly(player_id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_player_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(player_id) FROM stats_total WHERE player_id = v_player_id AND server = v_server) = 1 THEN
        UPDATE stats_total
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE player_id = v_player_id
          AND server = v_server;
    ELSE
        INSERT INTO stats_total(player_id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_player_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
END;

CREATE EVENT drop_monthly_stats ON SCHEDULE EVERY 1 MONTH
    STARTS DATE(CONCAT(YEAR(CURDATE()), '-', MONTH(CURDATE()) + 1, '-1')) DO
    BEGIN
        TRUNCATE stats_monthly;
    END;

CREATE EVENT drop_weekly_stats ON SCHEDULE EVERY 1 WEEK
    STARTS DATE(
            CONCAT(YEAR(CURDATE()), '-', MONTH(CURDATE()), '-',
                   DAY(CURDATE()) + 1 + (7 - (WEEKDAY(CURDATE()) + 1)))) DO
    BEGIN
        TRUNCATE stats_weekly;
    END;
