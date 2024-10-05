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
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BotsService {
    private Map<String, Function<String, BiFunction<GameService, String, GameAction>>> strategies = Map.of(
            "PassThenRandom", name -> statelessStrategy(new PassThenRandom(new Random(extractLong(name, "PassThenRandom\\(([^\\)]+)\\)", 1, 0)))::play)
    );
    private final GameService game;

    public void engage(String name, String gameId) {
        var strategyName = name.replaceFirst("^(?:bot:)?([^(:]*).*$", "$1");
        var strategyFactory = strategies.get(strategyName);
        if (strategyFactory == null) throw new IllegalArgumentException("Unknown strategy implementation for: " + name);
        var strategy = strategyFactory.apply(name);
        var t = new Thread(() -> {
            while(true) {
                var action = strategy.apply(game, name);
                game.playAndWait(name, action);
            }
        });
        t.setDaemon(true);
        t.setName(name);
        t.start();
    }

    public static BiFunction<GameService, String, GameAction> statelessStrategy(
            Function<List<GameAction>, GameAction> strategy
    ) {
        return ((gameService, playerName) -> strategy.apply(gameService.getValidActions(playerName)));
    }

    public static BiFunction<GameService, String, GameAction> strategy(
            BiFunction<PlayerState, List<GameAction>, GameAction> strategy
    ) {
        return ((gameService, playerName) -> strategy.apply(
                gameService.getStateNow(playerName),
                gameService.getValidActions(playerName)
        ));
    }


    public static long extractLong(String input, String regex, int group, long defaultValue) {
        var m = Pattern.compile(regex).matcher(input);
        if (!m.find()) return defaultValue;
        try {
            return Long.parseLong(m.group(group));
        } catch (Exception ignore) {
            return defaultValue;
        }
    }
}
