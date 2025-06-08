package me.aanchev.belotej.bots;

import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;

import java.util.List;

public interface BotStrategy {
    GameAction play(String gameId, PlayerState state, List<GameAction> validActions);

    public interface Stateless extends BotStrategy {
        @Override
        default GameAction play(String gameId, PlayerState state, List<GameAction> validActions) {
            return play(state, validActions);
        }

        GameAction play(PlayerState state, List<GameAction> validActions);
    }
}
