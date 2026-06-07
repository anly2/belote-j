package me.aanchev.belotej.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.serde.annotation.Serdeable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.domain.RelPlayer;
import me.aanchev.belotej.domain.WNES;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;
import static me.aanchev.belotej.engine.RelStateUtils.getPlayerState;
import static me.aanchev.belotej.engine.RelStateUtils.rotate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataGatheringService {
    @Property(name = "data-gathering.tracked-players")
    private final Set<String> trackedPlayers;
    @Property(name = "data-gathering.output-file-pattern", defaultValue = "data/%s.ndjson")
    private final String outputFilePattern;

    private final GameEngine engine;

    private ObjectMapper objectMapper = new ObjectMapper();
    private DateTimeFormatter nowFormatter = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();
    private Map<GameState, WNES<List<GameAction>>> histories = new WeakHashMap<>();


    public void handleAction(String playerName, GameState gameState, RelPlayer playerPos, GameAction action) {
        synchronized (gameState) {
            if (trackedPlayers.isEmpty()) return;

            if (trackedPlayers.contains(playerName)) {
                append(playerName, captureEvent(gameState, playerPos, action));
            }

            var history = histories.computeIfAbsent(gameState, k -> new WNES<>(
                    new ArrayList<>(10),
                    new ArrayList<>(10),
                    new ArrayList<>(10),
                    new ArrayList<>(10)
            ));
            history.get(playerPos).add(action);
        }
    }

    protected Object captureEvent(GameState gameState, RelPlayer player, GameAction action) {
        var now = nowFormatter.format(LocalDateTime.now());

        var playerState = getPlayerState(gameState, player);
        playerState.setPreviousTrick(null);
        playerState.setScore(null);

        var absHistory = histories.get(gameState);
        var history = absHistory == null ? null : rotate(absHistory, player.getIndex());

        var playable = engine.getValidActions(gameState, player);

        return new CapturedEvent(
                now,
                playerState,
                history,
                playable,
                action
        );
    }

    @Serdeable
    private record CapturedEvent(
        String when,
        PlayerState playerState,
        WNES<List<GameAction>> history,
        List<GameAction> playable,
        GameAction action
    ) {}



    @Blocking
    protected void append(String playerName, Object event) {
        var outputFile = Path.of(String.format(outputFilePattern, playerName));
        try {
            var parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String json = objectMapper.writeValueAsString(event);

            try (BufferedWriter writer = Files.newBufferedWriter(
                    outputFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )) {
                writer.write(json);
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Failed to persist tracked action event to {}", outputFile, e);
        }
    }
}
