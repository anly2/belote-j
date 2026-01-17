package me.aanchev.belotej.controllers;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.bots.BotsService;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.engine.GameLobby;
import me.aanchev.belotej.engine.GameService;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BeloteController {
    private final GameLobby gameLobby;
    private final GameService gameService;
    private final BotsService bots;

    @Named(TaskExecutors.BLOCKING)
    private final ExecutorService executorService;


    @Get("/{player}/game/create")
    public String createGame(
            @PathVariable String player,
            @QueryValue(defaultValue = "") String gameId, // This is convenient but before when it was forcebly UUID it could act like a password
            @QueryValue(defaultValue = "") String seed
    ) {
        return gameLobby.createGame(player, "".equals(seed) ? null : seed, "".equals(gameId) ? null : gameId);
    }

    @Get("/game/{gameId}/seed")
    public String getGameSeed(@PathVariable String gameId) {
        return gameLobby.getGameSeed(gameId);
    }

    @Get("/{player}/game/join/{gameId}")
    public String joinGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameLobby.joinGame(player, gameId);
    }

    @Get("/{player}/game/start/{gameId}")
    public String startGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameLobby.startGame(player, gameId);
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Get("/{player}/state")
    public CompletableFuture<PlayerState> state(
            @PathVariable String player,
            @QueryValue(defaultValue = "false") boolean waitForMyTurn
    ) {
        return CompletableFuture.supplyAsync(() -> gameService.getState(player, waitForMyTurn), executorService);
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Get("/{player}/play/{action}")
    public CompletableFuture<PlayerState> play(
            @PathVariable String player,
            @PathVariable String action,
            @QueryValue(defaultValue = "false") boolean waitForMyTurn
    ) {
        log.info("Player '{}' is playing: {}", player, action);
        return CompletableFuture.supplyAsync(() -> {
            gameService.play(player, GameAction.of(action), waitForMyTurn);
            return gameService.getState(player, waitForMyTurn);
        }, executorService);
    }

    @Get("/{player}/play")
    public List<GameAction> playable(@PathVariable String player) {
        return gameService.getValidActions(player);
    }

    @Get("/new")
    public HttpResponse<String> newGame(
            @QueryValue(defaultValue = "") String seed,
            @QueryValue(defaultValue = "") String gameName,  // This is convenient but before when it was forcebly UUID it could act like a password
            @QueryValue(defaultValue = "bot:PassThenRandom(123):") String botPrefix,
            @QueryValue(defaultValue = "") String south,
            @QueryValue(defaultValue = "") String west,
            @Parameter(example = "bot:RemoteCall(http://192.168.0.2:8000/play)")
            @QueryValue(defaultValue = "") String north,
            @QueryValue(defaultValue = "") String east
    ) {
        if (south == null) south = UUID.randomUUID().toString();
        if (west == null) west = botPrefix + UUID.randomUUID();
        if (north == null) north = botPrefix + UUID.randomUUID();
        if (east == null) east = botPrefix + UUID.randomUUID();


        var gameId = gameLobby.createGame(south, seed, gameName, true);
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