package me.aanchev.belotej.domain;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum Claim { // Declarations / Combinations
    TIERCE, // 20
    QUARTE, // 50
    QUINT, // 100
    BELOTE, // 20
    BRELAN, // 100
    BRELAN9, // 150
    BRELANJ; // 200

    public int getPoints() {
        return getPoints(this);
    }
    public static int getPoints(Claim claim) {
        return switch (claim) {
            case TIERCE, BELOTE -> 2;
            case QUARTE -> 5;
            case QUINT, BRELAN -> 10;
            case BRELAN9 -> 15;
            case BRELANJ -> 20;
        };
    }

}
