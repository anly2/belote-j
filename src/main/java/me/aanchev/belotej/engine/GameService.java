package me.aanchev.belotej.engine;

import io.micronaut.serde.annotation.Serdeable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static java.util.Optional.ofNullable;
import static me.aanchev.belotej.engine.RelStateUtils.getPlayerState;
import static me.aanchev.belotej.engine.RelStateUtils.rotate;
import static me.aanchev.utils.LatchUtils.await;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameLobby lobby;
    private final GameEngine engine;
    private final DataGatheringService dataGathering;



    private final Map<String, Sinks.Many<GameState>> streamsByGame =
            new ConcurrentHashMap<>();


    public Flux<PlayerState> streamState(String player) {
        var session = lobby.getGameSession(player);
        if (session == null) {
            throw new NoSuchElementException("Player '" + player + "' is not part of a game!");
        }

        GameState game = session.getValue();

        var gameStream = streamsByGame
                .computeIfAbsent(game.getGameId(), id -> Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false))
                .asFlux()
                .startWith(game);

        return gameStream.map(gameState -> getPlayerState(gameState, session.getKey()));
    }



    // Deprecated //

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

        if (waitForTurn) awaitTurn(player, session.getValue());

        return getPlayerState(session.getValue(), session.getKey());
    }

    public void awaitTurn(String player) {
        awaitTurn(player, lobby.getGameSession(player).getValue());
    }
    private void awaitTurn(String player, GameState game) {
        CountDownLatch latch;
        synchronized (game) {
            if (player.equals(getNextPlayerName(game))) {
                return;
            }
            latch = new CountDownLatch(1);
            waiters.put(player, latch);
        }
        await(latch);
    }

    public List<GameAction> getValidActions(String player) {
        var session = lobby.getGameSession(player);
        if (session == null) return null;

        return engine.getValidActions(session.getValue(), session.getKey());
    }


    public void play(String player, GameAction action) throws IllegalArgumentException, IllegalStateException {
        play(player, action, false);
    }
    public void playAndWait(String player, GameAction action) throws IllegalArgumentException, IllegalStateException {
        play(player, action, true);
    }

    public void play(String player, GameAction action, boolean wait) throws IllegalArgumentException, IllegalStateException {
        // FIXME: playAndWait when immediately own turn again fails?!
        var session = lobby.getGameSession(player);
        if (session == null) throw new NoSuchElementException("Player '"+player+"' is not part of a game!");

        GameState game = session.getValue();
        var latch = wait ? new CountDownLatch(1) : null;
        synchronized (game) {
            dataGathering.handleAction(player, game, session.getKey(), action);

            engine.play(game, session.getKey(), action);

            ofNullable(streamsByGame.get(game.getGameId())).ifPresent(stateStream ->
                    stateStream.tryEmitNext(game));

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


    public PreviousRoundInfo getPreviousRoundInfo(String player) {
        var session = lobby.getGameSession(player);
        if (session == null) return null;

        return getPreviousRoundInfo(session.getValue(), session.getKey());
    }

    public PreviousRoundInfo getPreviousRoundInfo(GameState gameState, RelPlayer position) {
        var lastTrick = gameState.getPreviousRoundLastTrick();
        if (lastTrick == null) return null;

        int rotation = position.getIndex();
        return new PreviousRoundInfo(
                rotate(lastTrick, rotation),
                rotate(gameState.getPreviousRoundPoints(), rotation)
        );
    }

    @Serdeable
    public record PreviousRoundInfo(WNES<Card> lastTrick, Scores points) {}
}
