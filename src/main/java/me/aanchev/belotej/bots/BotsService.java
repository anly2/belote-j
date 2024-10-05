package me.aanchev.belotej.bots;

import lombok.RequiredArgsConstructor;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.engine.GameService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class BotsService {
    private Map<String, BiFunction<PlayerState, List<GameAction>, GameAction>> strategies = Map.of(
            "PassThenRandom", new PassThenRandom(new Random(123))::gameAction
    );
    private final GameService game;

    public void engage(String name, String gameId) {
        var strategyName = name.replaceFirst("^(?:bot:)?([^:]*).*$", "$1");
        var strategy = strategies.get(strategyName);
        if (strategy == null) throw new IllegalArgumentException("Unknown strategy implementation for: " + name);
        var t = new Thread(() -> {
            while(true) {
                var state = game.getState(name, true);
                var action = strategy.apply(state, game.getValidActions(name));
                game.play(name, action);
            }
        });
        t.setDaemon(true);
        t.setName(name);
        t.start();
    }
}
