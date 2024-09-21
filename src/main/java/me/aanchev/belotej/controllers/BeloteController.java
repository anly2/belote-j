package me.aanchev.belotej.controllers;


import me.aanchev.belotej.domain.PlayerState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BeloteController {

    @GetMapping(value = "/{player}/state")
    public PlayerState state(
            @PathVariable String player,
            @RequestParam(defaultValue = "false") boolean waitForMyTurn
    ) {
        return null;
    }
}