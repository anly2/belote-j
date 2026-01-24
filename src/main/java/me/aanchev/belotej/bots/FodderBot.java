package me.aanchev.belotej.bots;

import lombok.AllArgsConstructor;
import me.aanchev.belotej.bots.BotStrategy.CallAndTrickStatelessBot;
import me.aanchev.belotej.domain.*;

import java.util.List;
import java.util.Random;

import static java.util.Comparator.comparingInt;
import static me.aanchev.belotej.domain.PassCall.PASS;


@AllArgsConstructor
public class FodderBot implements CallAndTrickStatelessBot {
    private Random random;

    @Override
    public GameAction playCalling(PlayerState state, List<TrumpCall> validActions) {
        var hand = state.getHand();

        // if got 3 jacks
        // if got 2 jacks and a 9
        // then: call ALL_TRUMPS
        var jacks = hand.stream().filter(c -> c.name().endsWith("J")).toList();
        var nines = hand.stream().filter(c -> c.name().endsWith("9")).toList();
        if (jacks.size() >= 3 || (jacks.size() == 2 && !nines.isEmpty())) {
            if (validActions.contains(Trump.J)) {
                return Trump.J;
            }
        }

        // if got 3 aces
        // then: call NO_TRUMPS
        var aces = hand.stream().filter(c -> c.name().endsWith("A")).toList();
        if (aces.size() >= 3) {
            if (validActions.contains(Trump.A)) {
                return Trump.A;
            }
        }

        // if got >= 4 cards of the same suit
        // if got 2 of the three strongest when the suit is trump (J,9,A) , and one more card of that suit
        // then: call the suit
        for (Trump suit : List.of(Trump.C, Trump.D, Trump.H, Trump.S)) {
            if (!validActions.contains(suit)) {
                continue;
            }

            var suitCards = hand.stream().filter(c -> c.getSuit() == suit).toList();
            if (suitCards.size() >= 4) {
                return suit;
            }

            if (suitCards.size() >= 3) {
                long strongCount = suitCards.stream()
                        .filter(c -> c.name().endsWith("J") || c.name().endsWith("9") || c.name().endsWith("A"))
                        .count();
                if (strongCount >= 2) {
                    return suit;
                }
            }
        }

        return PASS;
    }

    @Override
    public GameAction playTrick(PlayerState state, List<Card> validActions) {
        if (state.getTrickCurrentStrongestPlayer() == null) {
            return validActions.get(random.nextInt(validActions.size()));
        }

        var cards = validActions.stream()
                .sorted(comparingInt(c -> c.getPower(state.getTrump())))
                .toList();

        var teammateWinning = (state.getTrickCurrentStrongestPlayer() == RelPlayer.n);
        return teammateWinning ? cards.getLast() : cards.getFirst();
    }
}
