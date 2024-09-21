package me.aanchev.belotej.domain;

public enum Team {
    THEM, US;

    public static Team of(String value) {
        return switch (value.toUpperCase()) {
            case "THEM", "WE", "EW" -> THEM;
            case "US", "NS", "SN" -> US;
            default -> throw new IllegalArgumentException("Unknown team: " + value);
        };
    }
}
