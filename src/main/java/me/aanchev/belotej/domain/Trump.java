package me.aanchev.belotej.domain;

public enum Trump implements TrumpCall {
    C, D, H, S, A, J;

    public static Trump of(String value) {
        if (value == null) return null;
        String u = value.toUpperCase();
        if (u.startsWith("ALL")) return J;
        if (u.startsWith("NO")) return A;
        return Trump.valueOf(u.substring(0, 1));
    }
}
