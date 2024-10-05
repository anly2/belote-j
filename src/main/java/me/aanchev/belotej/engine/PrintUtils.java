package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.micronaut.core.util.CollectionUtils.last;
import static java.util.stream.Collectors.joining;
import static me.aanchev.belotej.domain.PassCall.PASS;
import static me.aanchev.belotej.domain.RelPlayer.*;
import static me.aanchev.belotej.domain.RelPlayer.e;

public class PrintUtils {
    public static void printBoard(GameLobby lobby, String player) {
        printBoard(lobby.getGameSession(player).getValue());
    }

//                 H9 +      |     . [3r 4r 5r oak QK]
//            H9        H9   |  H     .
//               ^ H9        |     .
//          [H7, H8, H9, H10, HJ, HQ, HK, HA]
    private static final String TEMPLATE = """
                   TN        |     n
              TW        TE   |  w     e
                   TS        |     s
            """;


    private static final int EST_LEN = TEMPLATE.length();
    public static void printBoard(GameState gameState) {
        var trickInitiator = gameState.getTrickInitiator();
        var winner = trickInitiator == null ? null : gameState.getTrickWinner();

        var sb = new StringBuilder(EST_LEN);
        sb.append("     ");
        sb.append(trickInitiator == n ? "^" : " ");
        sb.append(" ");
        sb.append(printable(gameState.getTrick().getN()));
        sb.append(" ");
        sb.append(winner == n ? "*" : " ");

        sb.append("      |     ");

        sb.append(printable(last(gameState.getCalls().getN())));

        var claimsN = gameState.getCombinations().getN();
        if (!claimsN.isEmpty())
            sb.append(" [").append(claimsN.stream().map(Map.Entry::getKey).map(PrintUtils::printable).collect(joining(" "))).append("]");

        sb.append("\n");

        sb.append(trickInitiator == w ? "^" : " ");
        sb.append(" ");
        sb.append(printable(gameState.getTrick().getW()));
        sb.append(" ");
        sb.append(winner == w ? "*" : " ");

        sb.append("    ");

        sb.append(trickInitiator == e ? "^" : " ");
        sb.append(" ");
        sb.append(printable(gameState.getTrick().getE()));
        sb.append(" ");
        sb.append(winner == e ? "*" : " ");

        sb.append(" |  ");

        sb.append(printable(last(gameState.getCalls().getW())));
        sb.append("     ");
        sb.append(printable(last(gameState.getCalls().getE())));

        var claimsW = gameState.getCombinations().getW();
        if (!claimsW.isEmpty())
            sb.append("  W:[").append(claimsW.stream().map(Map.Entry::getKey).map(PrintUtils::printable).collect(joining(" "))).append("]");
        var claimsE = gameState.getCombinations().getE();
        if (!claimsE.isEmpty())
            sb.append("  E:[").append(claimsE.stream().map(Map.Entry::getKey).map(PrintUtils::printable).collect(joining(" "))).append("]");

        sb.append("\n");

        sb.append("     ");
        sb.append(trickInitiator == s ? "^" : " ");
        sb.append(" ");
        sb.append(printable(gameState.getTrick().getS()));
        sb.append(" ");
        sb.append(winner == s ? "*" : " ");

        sb.append("      |     ");

        sb.append(printable(last(gameState.getCalls().getS())));

        var claimsS = gameState.getCombinations().getS();
        if (!claimsS.isEmpty())
            sb.append(" [").append(claimsS.stream().map(Map.Entry::getKey).map(PrintUtils::printable).collect(joining(" "))).append("]");



        System.out.println(sb);

        var hand = "[" + gameState.getHands().getS().stream().sorted().map(PrintUtils::printable).collect(joining(", ")) + "]";
        System.out.println(hand);

        System.out.println("---------------------------------");
    }


    private static String printable(Card card) {
        return card == null ? "  " : PRINT_TABLE_CARDS[card.ordinal()];
    }
    public static final String[] PRINT_TABLE_CARDS = {
            "♧7", "♧8", "♧9", "♧J", "♧Q", "♧K", "♧10", "♧A",
            "♦7", "♦8", "♦9", "♦J", "♦Q", "♦K", "♦10", "♦A",
            "♥7", "♥8", "♥9", "♥J", "♥Q", "♥K", "♥10", "♥A",
            "♤7", "♤8", "♤9", "♤J", "♤Q", "♤K", "♤10", "♤A",
    };


    private static String printable(TrumpCall trump) {
        return trump == null ? " " : PASS == trump ? "." : PRINT_TABLE_TRUMPS[((Trump) trump).ordinal()];
    }
    public static final String[] PRINT_TABLE_TRUMPS = {
            "♧", "♦","♥", "♤", "A", "J"
    };


    private static String printable(Claim claim) {
        return claim == null ? null : PRINT_TABLE_CLAIMS[claim.ordinal()];
    }
    public static final String[] PRINT_TABLE_CLAIMS = {
            "3r", "4r", "5r", "qk", "OAK", "9s", "Js"
    };
}
