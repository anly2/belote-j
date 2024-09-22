package me.aanchev.belotej.domain;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Objects;

@Serdeable
public enum Card implements GameAction {
    C7, C8, C9, CJ, CQ, CK, C10, CA,
    D7, D8, D9, DJ, DQ, DK, D10, DA,
    H7, H8, H9, HJ, HQ, HK, H10, HA,
    S7, S8, S9, SJ, SQ, SK, S10, SA;

    public static Card of(String value) {
        if (value == null) return null;
        if (value.endsWith("C") || value.endsWith("D") || value.endsWith("H") || value.endsWith("S")) {
            int p = value.length() - 1;
            return Card.of(value.substring(p) + value.substring(0, p));
        }
        return Card.valueOf(value.toUpperCase());
    }



    public final int getValue(Trump trump) {
        return getValue(this, trump);
    }
    public static int getValue(Card card, Trump trump) {
        return card.ordinal() + getValueBoost(card, trump);
    }

    public static int getValueBoost(Card card, Trump trump) {
        if (!isTrump(card, trump)) return 0;
        return switch (card) {
            case C9, D9, H9, S9 -> 150;
            case CJ, DJ, HJ, SJ -> 200;
            default -> 100;
        };
    }

    public final int getPower(Trump trump) {
        return getPower(this, trump);
    }
    public static int getPower(Card card, Trump trump) {
        if (card == null) return 0;
        return switch (card) {
            case C7, D7, H7, S7 -> 0;
            case C8, D8, H8, S8 -> 1;
            case C9, D9, H9, S9 -> isTrump(card, trump) ? 8 : 2;
            case CJ, DJ, HJ, SJ -> isTrump(card, trump) ? 9 : 3;
            case CQ, DQ, HQ, SQ -> 4;
            case CK, DK, HK, SK -> 5;
            case C10, D10, H10, S10 -> 6;
            case CA, DA, HA, SA -> 7;
        };
    }



    public final int getPoints(Trump trump) {
        return getPoints(this, trump);
    }
    public static int getPoints(Card card, Trump trump) {
        if (card == null) return 0;
        return switch (card) {
            case CQ, DQ, HQ, SQ -> 3;
            case CK, DK, HK, SK -> 4;
            case C10, D10, H10, S10 -> 10;
            case CA, DA, HA, SA -> 11;
            case C9, D9, H9, S9 -> isTrump(card, trump) ? 15 : 0;
            case CJ, DJ, HJ, SJ -> isTrump(card, trump) ? 20 : 2;
            default -> 0;
        };
    }


    public static boolean isTrump(Card card, Trump trump) {
        if (trump == Trump.A) return false;
        if (trump == Trump.J) return true;
        return trump == getSuit(card);
    }

    public Trump getSuit() {
        return getSuit(this);
    }
    public static Trump getSuit(Card card) {
        return switch (card) {
            case C7, C8, C9, CJ, CQ, CK, C10, CA -> Trump.C;
            case D7, D8, D9, DJ, DQ, DK, D10, DA -> Trump.D;
            case H7, H8, H9, HJ, HQ, HK, H10, HA -> Trump.H;
            case S7, S8, S9, SJ, SQ, SK, S10, SA -> Trump.S;
        };
    }

    public boolean sameSuit(Card other) {
        return sameSuit(this, other);
    }
    public static boolean sameSuit(Card a, Card b) {
        return getSuit(a) == getSuit(b);
    }


    public boolean sameKind(Card other) {
        return sameKind(this, other);
    }
    public static boolean sameKind(Card a, Card b) {
        return Objects.equals(
                a.name().substring(1),
                b.name().substring(1)
        );
    }
}
