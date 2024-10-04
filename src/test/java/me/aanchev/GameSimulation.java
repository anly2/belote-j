package me.aanchev;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import me.aanchev.belotej.controllers.BeloteController;
import me.aanchev.belotej.engine.GameLobby;
import org.junit.jupiter.api.Test;

import static me.aanchev.belotej.engine.PrintUtils.printBoard;

@MicronautTest
public class GameSimulation {

    @Inject
    BeloteController controller;
    @Inject
    GameLobby lobby;


    private static final String me = "South";
    private static final String foe1 = "West";
    private static final String friend = "North";
    private static final String foe2 = "East";

    @Test
    public void runSingleGame_allHumans() {
        var seed = "123";

        var gameId = controller.createGame(me, seed);
        controller.joinGame(foe1, gameId);
        controller.joinGame(friend, gameId);
        controller.joinGame(foe2, gameId);
        controller.startGame(me, gameId);

        printBoard(lobby, me);
    }


}
