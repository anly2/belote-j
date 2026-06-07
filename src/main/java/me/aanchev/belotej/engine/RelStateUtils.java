package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.*;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static me.aanchev.belotej.domain.WNES.wnes;

public class RelStateUtils {
    private RelStateUtils() {}


    public static PlayerState getPlayerState(GameState gameState, RelPlayer position) {
        int rotation = position.getIndex();
        WNES<Card> trick = gameState.getTrick();
        return PlayerState.builder()
                .dealer(rotate(gameState.getDealer(), rotation))
                .playerInTurn(rotate(gameState.getNext(), rotation))
                .hand(gameState.getHands().get(position))
                .calls(rotate(gameState.getCalls(), rotation))
                .trump(gameState.getTrump())
                .challengers(rotate(gameState.getChallengers(), rotation))
                .trick(rotate(trick, rotation))
                .trickInitiator(rotate(gameState.getTrickInitiator(), rotation))
                .trickAskingSuit(trick == null ? null :
                        ofNullable(gameState.getTrickInitiator()).map(trick::get).map(a -> a.getSuit()).orElse(null))
                .trickCurrentStrongestPlayer(rotate(gameState.getTrickWinner(), rotation))
                .trickCurrentStrongestCard(trick == null || gameState.getTrickWinner() == null ? null :
                        trick.get(gameState.getTrickWinner()))
                .previousTrick(rotate(gameState.getPreviousTrick(), rotation))
                .claims(rotate(gameState.getCombinations(), rotation)
                        .map(cs -> cs.stream().map(Map.Entry::getKey).toList()))
                .score(rotate(gameState.getScore(), rotation))
                .gameScore(rotate(gameState.getGameScore(), rotation))
                .build();
    }


    public static RelPlayer rotate(RelPlayer source, int observerIndex) {
        if (source == null) return null;
        return RelPlayer.get(source.getIndex() - observerIndex + 4);
    }

    public static Team rotate(Team source, int observerIndex) {
        if (source == null) return null;
        return switch (observerIndex % 4) {
            case 0, 2 -> source;
            case 1, 3 -> Team.other(source);
            default -> null; //not possible
        };
    }

    public static <E> WNES<E> rotate(WNES<E> source, int observerIndex) {
        if (source == null) return null;
        return switch (observerIndex % 4) {
            case 0 -> wnes(source.getW(), source.getN(), source.getE(), source.getS());
            case 1 -> wnes(source.getN(), source.getE(), source.getS(), source.getW());
            case 2 -> wnes(source.getE(), source.getS(), source.getW(), source.getN());
            case 3 -> wnes(source.getS(), source.getW(), source.getN(), source.getE());
            default -> null;
        };
    }

    public static Scores rotate(Scores source, int observerIndex) {
        if (source == null) return null;
        return switch (observerIndex % 4) {
            case 0, 2 -> new Scores(source.getUs(), source.getThem());
            case 1, 3 -> new Scores(source.getThem(), source.getUs());
            default -> null; //not possible
        };
    }
}
