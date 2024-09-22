package me.aanchev.belotej.domain;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum Team {
    THEM, US;

    public static Team of(RelPlayer player) {
        return switch (player) {
            case s, n -> US;
            case w, e -> THEM;
        };
    }

    public static Team of(String value) {
        return switch (value.toUpperCase()) {
            case "THEM", "WE", "EW" -> THEM;
            case "US", "NS", "SN" -> US;
            default -> throw new IllegalArgumentException("Unknown team: " + value);
        };
    }

    public static Team other(Team team) {
        return switch (team) {
            case US -> THEM;
            case THEM -> US;
        };
    }

    public static boolean sameTeam(RelPlayer a, RelPlayer b) {
        return Team.of(a) == Team.of(b);
    }
}
