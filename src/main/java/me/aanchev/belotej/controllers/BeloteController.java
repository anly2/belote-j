package me.aanchev.belotej.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.engine.GameLobby;
import me.aanchev.belotej.engine.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BeloteController {
    private final GameLobby gameLobby;
    private final GameService gameService;

    @GetMapping(value = "/{player}/game/create")
    public String createGame(
            @PathVariable String player,
            @RequestParam(required = false) String seed
    ) {
        return gameLobby.createGame(player, seed);
    }

    @GetMapping(value = "/{player}/game/join/{gameId}")
    public String joinGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameLobby.joinGame(player, gameId);
    }

    @GetMapping(value = "/{player}/game/start/{gameId}")
    public String startGame(
            @PathVariable String player,
            @PathVariable String gameId
    ) {
        return gameService.startGame(player, gameId);
    }

    @GetMapping(value = "/{player}/state")
    public PlayerState state(
            @PathVariable String player,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        return gameService.getState(player, waitForMyTurn);
    }

    @GetMapping(value = "/{player}/play/{action}")
    public PlayerState state(
            @PathVariable String player,
            @PathVariable String action,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        log.info("Player '{}' is playing: {}", player, action);
        gameService.play(player, GameAction.of(action), waitForMyTurn);
        return gameService.getState(player, waitForMyTurn);
    }
}