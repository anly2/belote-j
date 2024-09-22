package me.aanchev.belotej.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.engine.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BeloteController {
    private final GameService gameService;

    @GetMapping(value = "/{player}/state")
    public PlayerState state(
            @PathVariable String player,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        return gameService.getState(player, waitForMyTurn);
    }
}