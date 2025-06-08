package me.aanchev.belotej.bots;

import io.micronaut.serde.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;
import java.util.function.Function;

import static java.lang.Integer.parseInt;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteCallStrategyFactory {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public BotStrategy create(String remoteTarget) {
        var uri = URI.create(remoteTarget);
        return new RemoteCallStrategy(objectMapper, payload -> {
            try {
                String body = objectMapper.writeValueAsString(payload);
                log.debug("Attempting Remote Call Bot play via: {}{}", remoteTarget,
                        log.isTraceEnabled() ? " body: " + body : "");
                var req = HttpRequest.newBuilder().uri(uri).POST(ofString(body, UTF_8)).build();
                var response = httpClient.send(req, BodyHandlers.ofString()).body();
                log.debug("Received Remote Call Bot play: {}", response);
                return response;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}


@Slf4j
@RequiredArgsConstructor
class RemoteCallStrategy implements BotStrategy {
    private final ObjectMapper objectMapper;
    private final Function<Object, String> callingRemote;

    private String prevGameId = null;
    private List<WNES<Card>> previousTricks;

    @Override
    public GameAction play(String gameId, PlayerState state, List<GameAction> validActions) {
        var prevTrick = state.getPreviousTrick();
        if (!Objects.equals(prevGameId, gameId) || prevTrick == null) {
            log.debug("Resetting previous_tricks (gameId={})", gameId);
            prevGameId = gameId;
            previousTricks = new ArrayList<>(8);
        }
        if (prevTrick != null) previousTricks.add(prevTrick);

        var stateInfo = reshapeState(state, validActions);

        if (validActions.size() == 1) {
            log.debug("Only one valid action, so avoiding remote call and playing it directly.");
            return validActions.getFirst();
        }

        var _response = callingRemote.apply(stateInfo);
        var response = _response;

        try {
            var json = objectMapper.readValue(response, Map.class);
            var opt = json.get("option");
            if (opt instanceof String s) {
                response = s.trim();
            }
            if (opt instanceof Integer i) {
                response = String.valueOf(i);
            }
        }
        catch (Exception ignore) {
        }


        try {
            return validActions.get(parseInt(response));
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Attempted to play an unavailable option! Was it a zero-based index? Attempt: " + response + "; Valid options: " + validActions);
        }
        catch (Exception ignore) {}

        try {
            var desired = parseGameAction(response);
            if (validActions.contains(desired)) {
                return desired;
            }
            else {
                throw new IllegalStateException("Attempted to play an unavailable option! Was it a zero-based index? Attempt: " + response + " (" + desired + "); Valid options: " + validActions);
            }
        }
        catch (Exception ignore) {}

        throw new IllegalStateException("Could not handle remote call response!" +
                "It is neither an index of nor a valid string representation of a valid action to play:\n\t" + _response);
    }

    private LinkedHashMap<String, Object> reshapeState(PlayerState state, List<GameAction> validActions) {
        var info = new LinkedHashMap<String, Object>();

        info.put("bids", write(state.getCalls(), calls ->
                remap(calls, RemoteCallStrategy::write)));

        info.put("current_trump", write(state.getTrump()));

        info.put("combination_claims", write(state.getClaims(), claims ->
                remap(claims, RemoteCallStrategy::write)));

        info.put("previous_tricks", remap(previousTricks, trick ->
                write(trick, RemoteCallStrategy::write)));

        info.put("trick", write(state.getTrick(), RemoteCallStrategy::write));
        info.put("trick_initiator", write(state.getTrickInitiator()));
        info.put("trick_asking_suit", write(state.getTrickAskingSuit()));
        info.put("trick_current_strongest_card", write(state.getTrickCurrentStrongestCard()));
        info.put("trick_current_strongest_player", write(state.getTrickCurrentStrongestPlayer()));

        info.put("hand", remap(state.getHand(), RemoteCallStrategy::write));

        info.put("possible_actions", remap(validActions, RemoteCallStrategy::write));

        return info;
    }


    private static <E, R> List<R> remap(List<E> items, Function<E, R> remapper) {
        if (items == null) return null;
        return items.stream().map(remapper).toList();
    }

    private static Object write(WNES<?> value) {
        return write(value, e -> e);
    }
    private static <E> Object write(WNES<E> value, Function<E, ?> remapper) {
        if (value == null) return null;
        var result = new LinkedHashMap<String, Object>();
        result.put("SOUTH", remapper.apply(value.getW()));
        result.put("WEST", remapper.apply(value.getW()));
        result.put("NORTH", remapper.apply(value.getN()));
        result.put("EAST", remapper.apply(value.getE()));
        return result;
    }
    private static Object write(GameAction value) {
        if (value instanceof Trump trump) {
            return write(trump);
        }
        if (value instanceof Card card) {
            return write(card);
        }
        return value;
    }
    private static Object write(Trump value) {
        if (value == null) return null;
        return switch (value) {
            case C -> "CLUBS";
            case D -> "DIAMONDS";
            case H -> "HEARTS";
            case S -> "SPADES";
            case A -> "NO TRUMPS";
            case J -> "ALL TRUMPS";
        };
    }
    private static Object write(Card value) {
        if (value == null) return null;
        return value.name()
                .replaceFirst("^([CDHS])(.+)$", "$2$1")
                .replaceFirst("S$", " of SPADES") // must be first
                .replaceFirst("C$", " of CLUBS")
                .replaceFirst("D$", " of DIAMONDS")
                .replaceFirst("H$", " of HEARTS");
    }

    private static Object write(Claim value) {
        if (value == null) return null;
        return switch (value) {
            case BRELAN9 -> "BRELAN OF NINES";
            case BRELANJ -> "BRELAN OF JACKS";
            default -> value.name();
        };
    }

    private static Object write(RelPlayer value) {
        if (value == null) return null;
        return switch (value) {
            case s -> "SOUTH";
            case w -> "WEST";
            case n -> "NORTH";
            case e -> "EAST";
        };
    }


    private static GameAction parseGameAction(String value) {
        return GameAction.of(value
                .replace(" of CLUBS", "C")
                .replace(" of DIAMONDS", "D")
                .replace(" of HEARTS", "H")
                .replace(" of SPADES", "S")
                .replace("♣", "C").replace("♧", "C")
                .replace("♦", "D").replace("♢", "D")
                .replace("♥", "H").replace("♡", "H")
                .replace("♠", "S").replace("♤", "S")
                .replaceFirst("^(.+)([CDHS])$", "$2$1")
        );
    }
}
