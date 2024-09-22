package me.aanchev.belotej.engine;

import jakarta.annotation.Nullable;
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
public class GameLobby {
    private Map<String, Map.Entry<RelPlayer, GameState>> gamesByPlayer = new HashMap<>(1);

    protected Map.Entry<RelPlayer, GameState> getGameSession(String player) {
        return gamesByPlayer.get(player);
    }


    public String createGame(String player, @Nullable String seed) {
        GameState game = createGame(seed);
        addToGame(game, player);
        return game.getGameId();
    }

    public String joinGame(String player, String gameId) {
        var session = gamesByPlayer.values().stream().filter(g -> g.getValue().getGameId().equals(gameId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such game: " + gameId));
        var game = session.getValue();
        addToGame(game, player);
        return game.getGameId();
    }

    private void addToGame(GameState game, String player) {
        var players = game.getPlayerNames();
        if (players.contains(player)) return;
        var slot = players.indexOf(null);
        if (slot == -1) throw new IllegalStateException("No free slot available in game: " + game.getGameId());
        players.set(slot, player);
        gamesByPlayer.put(player, pair(RelPlayer.get(slot), game));
    }


    protected GameState createGame(@Nullable String seed) {
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
