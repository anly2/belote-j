package me.aanchev.belotej.engine;

import io.micronaut.runtime.event.annotation.EventListener;
import lombok.RequiredArgsConstructor;
import me.aanchev.belotej.domain.events.TrickEndedGameEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.print-on-trick-end", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TrickPrinter {
    private final GameLobby lobby;
    @EventListener
    public void onTrickEnded(TrickEndedGameEvent event) {
        PrintUtils.printBoard(lobby.getGame(event.gameId()));
    }
}
