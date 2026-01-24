package me.aanchev.belotej.bots;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.engine.GameService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotsService {
    private final GameService game;
    private final RemoteCallStrategyFactory remoteCallStrategyFactory;

    private Map<String, Function<String, BotStrategy>> strategies;
    @PostConstruct
    public void init() {
        // TODO: gather beans and build this strategy map from those instead of assembling it explicitly in this initializer
        strategies = Map.of(
                "PassThenRandom", name -> new PassThenRandom(
                        new Random(extractLong(name, "PassThenRandom\\(([^\\)]+)\\)", 1, 0))),
                "Fodder", name -> new FodderBot(
                        new Random(extractLong(name, "Fodder\\(([^\\)]+)\\)", 1, 0))),
                "RemoteCall", name -> remoteCallStrategyFactory.create(
                        extract(name, "RemoteCall\\(([^\\)]+)\\)", 1, null))
        );
    }

    public void engage(String name, String gameId) {
        var strategyName = name.replaceFirst("^(?:bot:)?([^(:]*).*$", "$1");
        var strategyFactory = strategies.get(strategyName);
        if (strategyFactory == null) throw new IllegalArgumentException("Unknown strategy implementation for: " + name);
        var strategy = strategyFactory.apply(name);
        var t = new Thread(() -> {
            while(true) {
                game.awaitTurn(name);
                log.debug("Calling player '{}' to play...", name);
                var action = strategy.play(gameId,
                        game.getStateNow(name),
                        game.getValidActions(name)
                );
                game.play(name, action);
            }
        });
        t.setDaemon(true);
        t.setName(name);
        t.start();
    }

    public static String extract(String input, String regex, int group, String defaultValue) {
        var m = Pattern.compile(regex).matcher(input);
        if (!m.find()) return defaultValue;
        return m.group(group);
    }
    public static long extractLong(String input, String regex, int group, long defaultValue) {
        try {
            return Long.parseLong(extract(input, regex, group, null));
        } catch (Exception ignore) {
            return defaultValue;
        }
    }
}
