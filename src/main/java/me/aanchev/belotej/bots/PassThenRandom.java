package me.aanchev.belotej.bots;

import lombok.AllArgsConstructor;
import me.aanchev.belotej.domain.GameAction;

import java.util.List;
import java.util.Random;

import static me.aanchev.belotej.domain.PassCall.PASS;

@AllArgsConstructor
public class PassThenRandom {
    private Random random;

    public GameAction play(List<GameAction> validActions) {
        if (validActions.contains(PASS)) return PASS;

        return validActions.get(random.nextInt(validActions.size()));
    }
}
