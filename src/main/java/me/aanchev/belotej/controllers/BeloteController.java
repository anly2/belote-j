package me.aanchev.belotej.controllers;


import io.micronaut.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.bots.BotsService;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.engine.GameLobby;
import me.aanchev.belotej.engine.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BeloteController {
    private final GameLobby gameLobby;
    private final GameService gameService;
    private final BotsService bots;

    @GetMapping("/{player}/game/create")
    public String createGame(
            @PathVariable String player,
            @RequestParam(required = false) String seed
    ) {
        return gameLobby.createGame(player, seed);
    }

    @GetMapping("/game/{gameId}/seed")
    public String getGameSeed(@PathVariable String gameId) {
        return gameLobby.getGameSeed(gameId);
    }

    @GetMapping("/{player}/game/join/{gameId}")
    public String joinGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameLobby.joinGame(player, gameId);
    }

    @GetMapping("/{player}/game/start/{gameId}")
    public String startGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameLobby.startGame(player, gameId);
    }

    @GetMapping("/{player}/state")
    public PlayerState state(
            @PathVariable String player,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        return gameService.getState(player, waitForMyTurn);
    }

    @GetMapping("/{player}/play/{action}")
    public PlayerState play(
            @PathVariable String player,
            @PathVariable String action,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        log.info("Player '{}' is playing: {}", player, action);
        gameService.play(player, GameAction.of(action), waitForMyTurn);
        return gameService.getState(player, waitForMyTurn);
    }

    @GetMapping("/{player}/play")
    public List<GameAction> playable(@PathVariable String player) {
        return gameService.getValidActions(player);
    }

    @GetMapping("/new")
    public HttpResponse<String> newGame(
            @RequestParam(required = false) String seed,
            @RequestParam(defaultValue = "bot:PassThenRandom(123):") String botPrefix,
            @RequestParam(required = false) String south,
            @RequestParam(required = false) String west,
            @RequestParam(required = false) String north,
            @RequestParam(required = false) String east
    ) {
        if (south == null) south = UUID.randomUUID().toString();
        if (west == null) west = botPrefix + UUID.randomUUID();
        if (north == null) north = botPrefix + UUID.randomUUID();
        if (east == null) east = botPrefix + UUID.randomUUID();


        var gameId = gameLobby.createGame(south, seed, true);
        engage("wait".equals(west) ? null : west, gameId);
        engage("wait".equals(north) ? null : north, gameId);
        engage("wait".equals(east) ? null : east, gameId);

        return HttpResponse.temporaryRedirect(URI.create("/ui/game-view.html?gameId=" + gameId + "&player=" + south));
    }

    private void engage(String player, String gameId) {
        gameLobby.joinGame(player, gameId);
        if (player != null && player.startsWith("bot:")) {
            bots.engage(player, gameId);
        }
    }
}