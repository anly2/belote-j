package me.aanchev.belotej.bots;

import lombok.AllArgsConstructor;
import me.aanchev.belotej.domain.GameAction;
import me.aanchev.belotej.domain.PlayerState;

import java.util.List;
import java.util.Random;

import static me.aanchev.belotej.domain.PassCall.PASS;

@AllArgsConstructor
public class PassThenRandom implements BotStrategy.Stateless {
    private Random random;

    @Override
    public GameAction play(PlayerState state, List<GameAction> validActions) {
        if (validActions.contains(PASS)) return PASS;

        return validActions.get(random.nextInt(validActions.size()));
    }
}
