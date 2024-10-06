package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.Claim;
import me.aanchev.belotej.domain.Trump;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static me.aanchev.belotej.domain.Card.*;
import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {
    private GameEngine sut = new GameEngine();

    @Test
    public void findCombination_none() {
        var cards = asList(C9, D10, H9, H10, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).isEmpty();
    }
    @Test
    public void findCombination_none_gap1() {
        var cards = asList(C9, D10, H9, HJ, HK, S9, SQ, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).isEmpty();
    }

    @Test
    public void findCombination_tierce() {
        var cards = asList(C9, D10, H9, HJ, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.TIERCE);
    }

    @Test
    public void findCombination_quarte() {
        var cards = asList(C9, D9, H10, HJ, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.QUARTE);
    }

    @Test
    public void findCombination_quarte_2() {
        var cards = asList(DQ, HK, S9, CK, C7, SQ, SJ, S10);
        var actual = sut.findClaims(cards, SJ,  Trump.S);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.QUARTE);
    }

    @Test
    public void findCombination_quint() {
        var cards = asList(D8, H9, H10, HJ, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.QUINT);
    }
    @Test
    public void findCombination_quint_with6() {
        var cards = asList(H8, H9, H10, HJ, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.QUINT);
    }

    @Test
    public void findCombination_fullHouse() {
        var cards = asList(H7, H8, H9, H10, HJ, HQ, HK, HA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactlyInAnyOrder(Claim.TIERCE, Claim.QUINT);
    }

    @Test
    public void findCombination_brelan() {
        var cards = asList(C10, D10, H10, HJ, HQ, S10, SK, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.BRELAN);
    }

    @Test
    public void findCombination_brelan9() {
        var cards = asList(C9, D9, DJ, H9, HQ, HK, S9, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.BRELAN9);
    }

    @Test
    public void findCombination_brelanJ() {
        var cards = asList(CJ, DJ, H9, HJ, HQ, HK, SJ, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactly(Claim.BRELANJ);
    }

    @Test
    public void findCombination_brelanAndRun() {
        var cards = asList(C10, D10, H10, HJ, HQ, HK, S10, SA);
        var actual = sut.findClaims(cards, HJ,  Trump.H);
        assertThat(actual).extracting(Map.Entry::getKey).containsExactlyInAnyOrder(Claim.BRELAN, Claim.TIERCE);
    }
}