package me.aanchev.belotej.engine;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.Card;
import me.aanchev.belotej.domain.RelPlayer;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingInt;
import static me.aanchev.utils.Bytes.indexOf;
import static me.aanchev.utils.DataUtils.pair;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameLobby {
    private Map<String, Map.Entry<RelPlayer, GameState>> gamesByPlayer = new HashMap<>(1);
    private Set<String> autostartGames = new HashSet<>(1);

    private final GameEngine engine;


    protected Map.Entry<RelPlayer, GameState> getGameSession(String player) {
        return gamesByPlayer.get(player);
    }


    public String createGame(String player, @Nullable String seed) {
        return createGame(player, seed, true);
    }
    public String createGame(String player, @Nullable String seed, boolean autostart) {
        GameState game = createGame(seed);
        addToGame(game, player, 0);
        log.info("Player '{}' created an new game: {}", player, game.getGameId());
        if (autostart) autostartGames.add(game.getGameId());
        return game.getGameId();
    }

    public String joinGame(String player, String gameId) {
        return joinGame(player, gameId, null);
    }
    public String joinGame(String player, String gameId, @Nullable RelPlayer position) {
        var session = gamesByPlayer.values().stream().filter(g -> g.getValue().getGameId().equals(gameId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such game: " + gameId));
        var game = session.getValue();
        addToGame(game, player, position == null ? -1 : position.getIndex());
        log.info("Player '{}' joined game: {}", player, game.getGameId());
        if (!game.getPlayerNames().contains(null) && autostartGames.contains(game.getGameId())) {
            engine.startRound(game);
        }
        return game.getGameId();
    }

    private void addToGame(GameState game, String player, int slot) {
        var players = game.getPlayerNames();
        if (slot < 0) {
            slot = players.indexOf(null);
            if (slot == -1) throw new IllegalStateException("No free slot available in game: " + game.getGameId());
        }
        var i = players.indexOf(player);
        if (i >= 0) players.set(i, null);
        players.set(slot, player);
        if (player != null) gamesByPlayer.put(player, pair(RelPlayer.get(slot), game));
    }

    public String startGame(String player, String gameId) {
        var session = getGameSession(player);
        var game = session.getValue();
        if (!Objects.equals(game.getGameId(), gameId)) throw new IllegalStateException("Wrong game: " + gameId);

        engine.startRound(game);
        return gameId;
    }


    protected GameState createGame(@Nullable String seed) {
        if (seed == null) seed = String.valueOf(new Random().nextLong());

        try {
            return createGame(Long.parseLong(seed));
        } catch (Exception ignore) {}

        try {
            byte[] bytes = Base64.getDecoder().decode(seed);
            if (bytes.length != 32) throw new IllegalArgumentException("Not a byte array holding the card indices");
            return createGame(bytes);
        } catch (Exception ignore) {}

        return createGame(seed.hashCode());
    }

    protected GameState createGame(long seed) {
        var deck = new ArrayList<>(asList(Card.values()));
        Random random = new Random(seed);
        shuffle(deck, random);
        return GameState.newGame(deck, random);
    }
    protected GameState createGame(byte[] seed) {
        var deck = new ArrayList<>(asList(Card.values()));
        deck.sort(comparingInt(card -> indexOf(seed, (byte) card.ordinal())));
        return GameState.newGame(deck);
    }
}
