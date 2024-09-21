package me.aanchev.belotej.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.aanchev.belotej.domain.*;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static me.aanchev.belotej.domain.RelPlayer.e;
import static me.aanchev.belotej.domain.RelPlayer.s;
import static me.aanchev.belotej.domain.WNES.wnes;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class GameState {
    private final String gameId;
    private final List<String> players;
    private final List<Card> initialDeck;

    private List<Card> deck = new ArrayList<>(32);

    private RelPlayer next = s;
    private RelPlayer dealer = e;

    private WNES<List<Card>> hands = wnes(() -> new ArrayList<>(8));

    private WNES<List<TrumpCall>> calls = wnes(() -> new ArrayList<>(1));
    private Trump trump = null;
    private Team challengers = null;


    private WNES<Card> trick = wnes();
    private WNES<List<Card>> winPiles = wnes(() -> new ArrayList<>(32));
    private WNES<List<Map.Entry<Claim, List<Card>>>> combinations = wnes(() -> new ArrayList<>(2));


    private Scores score = new Scores();
    private Scores gameScore = new Scores();



    public void resetGame() {
        deck.clear();
        deck.addAll(initialDeck);
        clearTrick();
        gameScore.reset();
    }

    public void clearTrick() {
        clear(hands);
        clear(calls);

        trump = null;
        challengers = null;

        trick.reset();
        clear(winPiles);

        score.reset();
    }
    public static <E> void clear(WNES<List<E>> state) {
        state.getW().clear();
        state.getN().clear();
        state.getE().clear();
        state.getS().clear();
    }


    public static GameState newGame(long seed) {
        var deck = new ArrayList<>(asList(Card.values()));
        shuffle(deck, new Random(seed));
        return newGame(deck);
    }
    public static GameState newGame(List<Card> deck) {
        return newGame(deck, "South");
    }
    public static GameState newGame(List<Card> deck, String playerSouthName) {
        return newGame(deck, playerSouthName, UUID.randomUUID().toString());
    }
    public static GameState newGame(List<Card> deck, String playerSouthName, String gameId) {
        return newGame(deck, List.of(playerSouthName, "West", "North", "East"), gameId);
    }
    public static GameState newGame(List<Card> deck, List<String> playerNames, String gameId) {
        return new GameState(gameId, playerNames, deck);
    }
}
