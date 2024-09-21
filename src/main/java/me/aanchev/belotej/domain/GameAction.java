package me.aanchev.belotej.domain;

public interface GameAction {
    public static GameAction of(String value) {
        if (value == null) return null;
        return switch (value.length()) {
            case 1 -> Trump.of(value);
            case 2, 3 -> Card.of(value);
            case 4 -> PassCall.valueOf(value.toUpperCase());
            default -> {
                try {
                    Claim.valueOf(value.toUpperCase());
                } catch (Exception ignore) {}
                throw new IllegalArgumentException("Not a valid game action: " + value);
            }
        };
    }
}
