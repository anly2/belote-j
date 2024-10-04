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
    private static final GameEngine ge = new GameEngine(); // FIXME: winner as state
    public static void printBoard(GameState gameState) {
        var trickInitiator = gameState.getTrickInitiator();
        var winner = trickInitiator == null ? null : ge.getTrickWinner(gameState); // FIXME

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



    private static final int iTS = TEMPLATE.indexOf("TS");
    private static final int iTW = TEMPLATE.indexOf("TW");
    private static final int iTN = TEMPLATE.indexOf("TN");
    private static final int iTE = TEMPLATE.indexOf("TE");
    private static final int eTS = iTS + 2;
    private static final int eTW = iTW + 2;
    private static final int eTN = iTN + 2;
    private static final int eTE = iTE + 2;
    private static final int iBS = TEMPLATE.indexOf("s");
    private static final int iBW = TEMPLATE.indexOf("w");
    private static final int iBN = TEMPLATE.indexOf("n");
    private static final int iBE = TEMPLATE.indexOf("e");
    private static final int eBS = iBS + 1;
    private static final int eBW = iBW + 1;
    private static final int eBN = iBN + 1;
    private static final int eBE = iBE + 1;
    private static final int aCS = eBS;
    private static final int aCW = eBE; // also HERE!
    private static final int aCN = eBN;
    private static final int aCE = eBE;

    public static void printBoard_(GameState gameState) {
        var result = new StringBuffer(TEMPLATE);
        result.replace(iTS, eTS, printable(gameState.getTrick().getS()));
        result.replace(iTW, eTW, printable(gameState.getTrick().getW()));
        result.replace(iTN, eTN, printable(gameState.getTrick().getN()));
        result.replace(iTE, eTE, printable(gameState.getTrick().getE()));

        result.replace(iBS, eBS, printable(last(gameState.getCalls().getS())));
        result.replace(iBW, eBW, printable(last(gameState.getCalls().getW())));
        result.replace(iBN, eBN, printable(last(gameState.getCalls().getN())));
        result.replace(iBE, eBE, printable(last(gameState.getCalls().getE())));

        System.out.println(result);

        var hand = "[" + gameState.getHands().getS().stream().map(PrintUtils::printable).collect(joining(", ")) + "]";
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
