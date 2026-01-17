package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.Card;
import me.aanchev.belotej.domain.Trump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static me.aanchev.belotej.domain.Card.*;
import static org.assertj.core.api.Assertions.assertThat;

class PlayableCardsSortingTest {

    @Test
    public void cards_shouldSortByPower_keepingSuitsAsGroups() {
        // Wrong: C7, D8, DA, C9, D9, CJ (out of order, disjointed suits)
        var cards = new ArrayList<>(asList(C7, D8, DA, C9, D9, CJ));
        
        var trump = Trump.J; // All trumps
        cards.sort(comparingInt(c -> Card.getValue(c, trump)));
        
        // Correct: (suits stay as groups)
        assertThat(cards).containsExactly(C7, C9, CJ, D8, DA, D9);
    }

    @Test
    public void cards_shouldSortByPower_maybeHoistingTheTrump() {
        // Wrong: C7, D8, DA, C9, D9, CJ
        var cards = new ArrayList<>(asList(C7, D8, DA, C9, D9, CJ));

        var trump = Trump.C; // Clubs (normally weaker than Diamonds)
        cards.sort(comparingInt(c -> Card.getValue(c, trump)));

        // Correct, if the Trump suit is "hoisted"
        //assertThat(cards).containsExactly(D8, D9, DA, C7, C9, CJ);

        // Correct, if no "hoisting" (but doesn't really matter anyway)
        assertThat(cards).containsExactly(C7, C9, CJ, D8, D9, DA);
    }
}
