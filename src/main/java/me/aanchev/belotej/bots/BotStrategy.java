package me.aanchev.belotej.bots;

import me.aanchev.belotej.domain.Card;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;
import me.aanchev.belotej.domain.TrumpCall;

import java.util.List;

import static me.aanchev.belotej.domain.PassCall.PASS;

public interface BotStrategy {
    GameAction play(String gameId, PlayerState state, List<GameAction> validActions);



    public interface Stateless extends BotStrategy {
        @Override
        default GameAction play(String gameId, PlayerState state, List<GameAction> validActions) {
            return play(state, validActions);
        }

        GameAction play(PlayerState state, List<GameAction> validActions);
    }


    public interface CallAndTrickStatelessBot extends Stateless {
        default GameAction play(PlayerState state, List<GameAction> validActions) {
            if (validActions.contains(PASS)) {
                return playCalling(state, validActions
                        .stream().map(TrumpCall.class::cast).toList());
            }
            return playTrick(state, validActions
                    .stream().map(Card.class::cast).toList());
        }

        GameAction playCalling(PlayerState state, List<TrumpCall> validActions);
        GameAction playTrick(PlayerState state, List<Card> validActions);
    }
}
