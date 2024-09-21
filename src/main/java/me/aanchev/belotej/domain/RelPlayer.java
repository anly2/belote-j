package me.aanchev.belotej.domain;

public enum RelPlayer {
    s, w, n, e;

    public final RelPlayer next() {
        return switch (this) {
            case s -> w;
            case w -> n;
            case n -> e;
            case e -> s;
        };
    }

    public static RelPlayer get(int index) {
        return switch (index % 4) {
            case 0 -> s;
            case 1 -> w;
            case 2 -> n;
            case 3 -> e;
            default -> null; // not possible
        };
    }
}
