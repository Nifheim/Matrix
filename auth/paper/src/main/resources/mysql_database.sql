-- drop table
DROP TABLE IF EXISTS player_identity;

-- create table
CREATE TABLE IF NOT EXISTS player_identity
(
    id            UUID         NOT NULL PRIMARY KEY DEFAULT UUID(),
    player_id     UUID         NOT NULL UNIQUE REFERENCES player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    password_hash CHAR(128)    NOT NULL,
    password_salt CHAR(24)     NOT NULL,
    rounds        INT UNSIGNED NOT NULL,
    algorithm     VARCHAR(50)  NOT NULL
)
