package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.RelPlayer;
import me.aanchev.belotej.domain.Trump;
import me.aanchev.belotej.domain.WNES;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static me.aanchev.belotej.domain.Card.*;
import static org.assertj.core.api.Assertions.assertThat;

class GameEngineCanPlayTests {
    private GameEngine sut = new GameEngine();

    @Test
    public void canPlay_suitTrump_teammateHolding_shouldStillRaise() {
        var state = new GameLobby(sut).createGame(123, "1");

        state.setTrump(Trump.S);
        state.setTrick(WNES.wnes(S8, S9, SK, null));
        state.setTrickInitiator(RelPlayer.w);
        state.setTrickWinner(RelPlayer.n);

        state.getHands().setS(asList(C7, C8, CQ, D10, DK, S10, S7, SJ));

        assertThat(sut.canPlay(state, S7, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, S10, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, SJ, RelPlayer.s)).isTrue();
    }

    @Test
    public void canPlay_suitNotTrump_raisedToTrump_cannotRaiseMore_shouldBeAbleToPlayNonTrump() {
        var state = new GameLobby(sut).createGame(123, "1");

        state.setTrump(Trump.S);
        state.setTrick(WNES.wnes(null, D7, SA, null));
        state.setTrickInitiator(RelPlayer.n);
        state.setTrickWinner(RelPlayer.e);

        state.getHands().setS(asList(C7, SQ));

        assertThat(sut.canPlay(state, C7, RelPlayer.s)).isTrue();
        assertThat(sut.canPlay(state, SQ, RelPlayer.s)).isTrue();
    }

    @Test
    public void canPlay_suitNotTrump_raisedToNotTrump_shouldPlayTrump() {
        var state = new GameLobby(sut).createGame(123, "1");

        state.setTrump(Trump.S);
        state.setTrick(WNES.wnes(null, D8, DK, null));
        state.setTrickInitiator(RelPlayer.n);
        state.setTrickWinner(RelPlayer.e);

        state.getHands().setS(asList(H9, HJ, S8, SQ));

        assertThat(sut.canPlay(state, H9, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, HJ, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, S8, RelPlayer.s)).isTrue();
        assertThat(sut.canPlay(state, SQ, RelPlayer.s)).isTrue();
    }
}