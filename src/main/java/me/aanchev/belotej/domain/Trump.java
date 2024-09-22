package me.aanchev.belotej.domain;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum Trump implements TrumpCall {
    C, D, H, S, A, J;

    public static Trump of(String value) {
        if (value == null) return null;
        String u = value.toUpperCase();
        if (u.startsWith("ALL")) return J;
        if (u.startsWith("NO")) return A;
        return Trump.valueOf(u.substring(0, 1));
    }



    public boolean isTrump(Trump trump) {
        return isTrump(this, trump);
    }
    public static boolean isTrump(Trump suit, Trump trump) {
        if (trump == Trump.A) return false;
        if (trump == Trump.J) return true;
        return trump == suit;
    }
}
