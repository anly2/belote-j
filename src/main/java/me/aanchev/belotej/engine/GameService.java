package me.aanchev.belotej.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static me.aanchev.belotej.domain.WNES.wnes;
import static me.aanchev.utils.LatchUtils.await;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameLobby lobby;
    private final GameEngine engine;

    private Map<String, CountDownLatch> waiters = new ConcurrentHashMap<>(4);


    public PlayerState getStateNow(String player) {
        return getState(player, false);
    }
    public PlayerState getStateWhenTheirTurn(String player) {
        return getState(player, true);
    }
    public PlayerState getState(String player, boolean waitForTurn) {
        var session = lobby.getGameSession(player);
        if (session == null) return null;

        if (waitForTurn && !player.equals(getNextPlayerName(session.getValue()))) {

            var latch = new CountDownLatch(1);
            waiters.put(player, latch);
            await(latch);
        }

        return getPlayerState(session.getValue(), session.getKey());
    }

    public List<GameAction> getValidActions(String player) {
        var session = lobby.getGameSession(player);
        if (session == null) return null;

        return engine.getValidActions(session.getValue(), session.getKey());
    }

    protected PlayerState getPlayerState(GameState gameState, RelPlayer position) {
        int rotation = position.getIndex();
        return PlayerState.builder()
                .dealer(rotate(gameState.getDealer(), rotation))
                .playerInTurn(rotate(gameState.getNext(), rotation))
                .hand(gameState.getHands().get(position))
                .calls(rotate(gameState.getCalls(), rotation))
                .trump(gameState.getTrump())
                .challengers(rotate(gameState.getChallengers(), rotation))
                .trick(rotate(gameState.getTrick(), rotation))
                .claims(rotate(gameState.getCombinations(), rotation)
                        .map(cs -> cs.stream().map(Map.Entry::getKey).toList()))
                .score(rotate(gameState.getScore(), rotation))
                .gameScore(rotate(gameState.getGameScore(), rotation))
                .build();
    }



    public void play(String player, GameAction action) throws IllegalArgumentException, IllegalStateException {
        play(player, action, false);
    }
    public void playAndWait(String player, GameAction action) throws IllegalArgumentException, IllegalStateException {
        play(player, action, true);
    }

    public void play(String player, GameAction action, boolean wait) throws IllegalArgumentException, IllegalStateException {
        var session = lobby.getGameSession(player);
        if (session == null) throw new NoSuchElementException("Player '"+player+"' is not part of a game!");

        GameState game = session.getValue();
        var latch = wait ? new CountDownLatch(1) : null;
        synchronized (game) {
            engine.play(game, session.getKey(), action);

            String nextPlayer = getNextPlayerName(game);
            var nextPlayerLatch = nextPlayer != null ? waiters.get(nextPlayer) : null;
            if (nextPlayerLatch != null) nextPlayerLatch.countDown();

            if (wait) waiters.put(player, latch);
            else waiters.remove(player);
        }

        if (wait) await(latch);
    }

    private static String getNextPlayerName(GameState game) {
        var nextPlayerIndex = game.getNext().getIndex();
        var nextPlayer = nextPlayerIndex < game.getPlayerNames().size() ? game.getPlayerNames().get(nextPlayerIndex) : null;
        return nextPlayer;
    }


    public static RelPlayer rotate(RelPlayer source, int offset) {
        return RelPlayer.get(source.getIndex() + offset);
    }


    public static Team rotate(Team source, int rotation) {
        return switch (rotation % 4) {
            case 0, 2 -> source;
            case 1, 3 -> Team.other(source);
            default -> null; //not possible
        };
    }

    public static <E> WNES<E> rotate(WNES<E> source, int rotation) {
        return switch (rotation % 4) {
            case 0 -> wnes(source.getW(), source.getN(), source.getE(), source.getS());
            case 1 -> wnes(source.getN(), source.getE(), source.getS(), source.getW());
            case 2 -> wnes(source.getE(), source.getS(), source.getW(), source.getN());
            case 3 -> wnes(source.getS(), source.getW(), source.getN(), source.getE());
            default -> null; //not possible
        };
    }


    public static Scores rotate(Scores source, int rotation) {
        return switch (rotation % 4) {
            case 0, 2 -> new Scores(source.getUs(), source.getThem());
            case 1, 3 -> new Scores(source.getThem(), source.getUs());
            default -> null; //not possible
        };
    }
}
