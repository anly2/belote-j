package me.aanchev.belotej.domain;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
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

    public final RelPlayer previous() {
        return switch (this) {
            case s -> e;
            case w -> s;
            case n -> w;
            case e -> n;
        };
    }


    public final int getIndex() {
        return switch (this) {
            case s -> 0;
            case w -> 1;
            case n -> 2;
            case e -> 3;
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
