package me.aanchev.belotej.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.aanchev.belotej.domain.*;

import java.util.*;

import static java.util.Arrays.asList;
import static me.aanchev.belotej.domain.RelPlayer.e;
import static me.aanchev.belotej.domain.RelPlayer.s;
import static me.aanchev.belotej.domain.WNES.wnes;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
class GameState {
    private final String gameId;
    private final List<Card> initialDeck;
    private final Random randomSeed;

    private List<String> playerNames = new ArrayList<>(asList(null, null, null, null));

    private List<Card> deck = new ArrayList<>(32);

    private RelPlayer next = s;
    private RelPlayer dealer = e;

    private WNES<List<Card>> hands = wnes(() -> new ArrayList<>(8));
    private WNES<List<Card>> playable = wnes(() -> new ArrayList<>(8));

    private WNES<List<TrumpCall>> calls = wnes(() -> new ArrayList<>(1));
    private TrumpCall winningCall = null;
    private Trump trump = null;
    private Team challengers = null;


    private WNES<Card> previousTrick = wnes();
    private WNES<Card> trick = wnes();
    private RelPlayer trickInitiator = null;
    private RelPlayer trickWinner = null;
    private WNES<List<Card>> winPiles = wnes(() -> new ArrayList<>(32));
    private WNES<List<Map.Entry<Claim, List<Card>>>> combinations = wnes(() -> new ArrayList<>(2));


    private Scores score = new Scores();
    private Scores gameScore = new Scores();



    public void resetGame() {
        deck.clear();
        deck.addAll(initialDeck);
        clearRound();
        gameScore.reset();
    }

    public void clearRound() {
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


    public static GameState newGame(List<Card> deck) {
        return newGame(deck, new Random());
    }
    public static GameState newGame(List<Card> deck, Random seed) {
        return newGame(deck, seed, UUID.randomUUID().toString());
    }
    public static GameState newGame(List<Card> deck, Random seed, String gameId) {
        return new GameState(gameId, deck, seed);
    }
}
