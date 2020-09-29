package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.server.GameType;
import java.util.Objects;

/**
 * @author Beelzebu
 */
public enum GameTypeImpl implements GameType {

    TOWNY("towny"),
    @Deprecated
    CHETO("cheto"),
    SURVIVAL("survival"),
    SKYWARS("skywars"),
    SKYBLOCK("skyblock"),
    BEDWARS("bedwars"),
    EGGWARS("eggwars"),
    KITPVP("kitpvp"),
    RFTB("rftb"),
    SSG("ssg"),
    FULLPVP("fullpvp"),
    TNTRUN("tntrun"),
    BUILDBATTLE("buildbattle"),
    UHC("uhc"),
    INFLUENCER("influencer"),
    THE_PIT("thepit"),
    @Deprecated
    ONE_BLOCK("oneblock"),
    ONEBLOCK("oneblock"),
    MODS("mods"),
    ARCADE("arcade"),
    CREATIVE("creative"),
    ANARCHIC("anarchic"),
    ROLEPLAY("roleplay"),
    RPG("rpg"),
    ISLANDS("islands"),
    PIXELMON("pixelmon"),
    FACTIONS("factions"),
    AMONGUS("amongas"),
    FALL_GUYS("wc"),
    SURVILOBBY("survilobby"),
    CHILETUMARE("chiletumare"),
    HARDCORE_CLANS("clans"),
    PRISON("prison");

    private final String gameName;

    GameTypeImpl(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public static GameType getByName(String name) {
        Objects.requireNonNull(name, "Name can't be null");
        try {
            return GameTypeImpl.valueOf(name);
        } catch (IllegalArgumentException e) {
            for (GameType gameType : values()) {
                if (gameType.getGameName().equals(name)) {
                    return gameType;
                }
            }
        }
        return NONE;
    }

    @Override
    public String toString() {
        return gameName;
    }
}