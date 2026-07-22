-- schema
CREATE DATABASE IF NOT EXISTS matrix;
USE matrix;
-- enable scheduler
SET GLOBAL event_scheduler = ON;
-- servers tables
DROP TABLE IF EXISTS failed_login;
DROP TABLE IF EXISTS command_log;
DROP TABLE IF EXISTS server;
DROP TABLE IF EXISTS server_group;
--
-- players and sessions
DROP TABLE IF EXISTS player_locale;
DROP TABLE IF EXISTS locale;
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
DROP TABLE IF EXISTS player_identity;
DROP TABLE IF EXISTS handshake;
DROP TABLE IF EXISTS player;

-- create servers tables
CREATE TABLE IF NOT EXISTS server_group
(
    id          INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    name        VARCHAR(20)  NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS server
(
    id          INT UNSIGNED                                        NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    group_id    INT UNSIGNED                                        NOT NULL REFERENCES server_group (id) ON DELETE CASCADE ON UPDATE CASCADE,
    name        VARCHAR(50)                                         NOT NULL,
    description VARCHAR(255)                                        NOT NULL,
    status      ENUM ('OPEN', 'WHITELIST', 'MAINTENANCE', 'CLOSED') NOT NULL                            DEFAULT 'MAINTENANCE',
    UNIQUE KEY uq_server (group_id, name)
);

-- create players
CREATE TABLE IF NOT EXISTS locale
(
    id   INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    code CHAR(4)      NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS player
(
    id            BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    uniqueId      UUID            NOT NULL UNIQUE,
    discordId     BIGINT UNSIGNED NULL UNIQUE                         DEFAULT NULL,
    name          VARCHAR(16)     NOT NULL COLLATE utf8mb4_unicode_ci,
    display_name  VARCHAR(128)    NULL                                DEFAULT NULL,
    premium       BOOLEAN         NOT NULL                            DEFAULT FALSE,
    registered    BOOLEAN         NOT NULL                            DEFAULT FALSE,
    locale        CHAR(5)         NULL,
    registered_at TIMESTAMP       NULL,
    created_at    TIMESTAMP       NOT NULL                            DEFAULT NOW(),
    updated_at    TIMESTAMP       NULL                                DEFAULT NULL ON UPDATE NOW()
);

CREATE TABLE IF NOT EXISTS player_locale
(
    id         BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    player_id  BIGINT UNSIGNED NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    locale_id  INT UNSIGNED    NOT NULL REFERENCES locale (id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at TIMESTAMP       NOT NULL                            DEFAULT NOW()
);

-- login and sessions
CREATE TABLE IF NOT EXISTS handshake
(
    id        BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    player_id BIGINT UNSIGNED NULL REFERENCES player (id) ON UPDATE CASCADE,
    ip        VARCHAR(15)     NOT NULL,
    protocol  INT UNSIGNED    NOT NULL,
    version   VARCHAR(32)     NOT NULL,
    hostname  VARCHAR(255)    NULL,
    timestamp TIMESTAMP       NOT NULL                            DEFAULT NOW(),
    FULLTEXT (ip)
);

CREATE TABLE IF NOT EXISTS player_login
(
    id           BIGINT UNSIGNED                                         NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    handshake_id BIGINT UNSIGNED                                         NOT NULL REFERENCES handshake (id) ON DELETE CASCADE ON UPDATE CASCADE,
    state        ENUM ('PRE_LOGIN', 'LOGIN', 'POST_LOGIN', 'DISCONNECT') NOT NULL,
    timestamp    TIMESTAMP                                               NOT NULL                            DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS failed_login
(
    id        BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    server_id INT UNSIGNED    NOT NULL REFERENCES server (id) ON DELETE CASCADE ON UPDATE CASCADE,
    player_id BIGINT UNSIGNED NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    uniqueId  UUID            NOT NULL,
    name      VARCHAR(16)     NOT NULL,
    ip        VARCHAR(15)     NOT NULL,
    message   TEXT            NULL                                DEFAULT NULL,
    timestamp TIMESTAMP       NOT NULL                            DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS command_log
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT DEFAULT NULL,
    player_id    BIGINT UNSIGNED NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    server_id    INT UNSIGNED    NOT NULL REFERENCES server (id) ON DELETE CASCADE ON UPDATE CASCADE,
    command      VARCHAR(256)    NOT NULL,
    full_command LONGTEXT        NOT NULL,
    date         DATETIME        NOT NULL                            DEFAULT SYSDATE()
);

CREATE OR REPLACE TRIGGER tr_player_locale_update
    AFTER UPDATE
    ON player
    FOR EACH ROW
BEGIN
    INSERT INTO locale(code) VALUES (new.locale) ON DUPLICATE KEY UPDATE id = id;
    INSERT INTO player_locale(player_id, locale_id) VALUES (new.id, (SELECT id FROM locale WHERE code = new.locale));
END;

CREATE OR REPLACE TRIGGER tr_player_locale_insert
    AFTER INSERT
    ON player
    FOR EACH ROW
BEGIN
    INSERT INTO locale(code) VALUES (new.locale) ON DUPLICATE KEY UPDATE id = id;
    INSERT INTO player_locale(player_id, locale_id) VALUES (new.id, (SELECT id FROM locale WHERE code = new.locale));
END;

-- stats
-- create stats tables
/*
CREATE TABLE IF NOT EXISTS stats_total
(
    player_id    BIGINT UNSIGNED NOT NULL REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    server       VARCHAR(50)     NOT NULL,
    kills        BIGINT UNSIGNED NOT NULL DEFAULT 0,
    mobKills     BIGINT UNSIGNED NOT NULL DEFAULT 0,
    deaths       BIGINT UNSIGNED NOT NULL DEFAULT 0,
    blocksBroken BIGINT UNSIGNED NOT NULL DEFAULT 0,
    blocksPlaced BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY pk_user_server (player_id, server)
);

CREATE TABLE IF NOT EXISTS stats_monthly LIKE stats_total;

CREATE TABLE IF NOT EXISTS stats_weekly LIKE stats_total;

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

 */
