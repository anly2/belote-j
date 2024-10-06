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
        var state = new GameLobby(sut).createGame(123);

        state.setTrump(Trump.S);
        state.setTrick(WNES.wnes(S8, S9, SK, null));
        state.setTrickInitiator(RelPlayer.w);
        state.setTrickWinner(RelPlayer.n);

        state.getHands().setS(asList(C7, C8, CQ, D10, DK, S10, S7, SJ));

        assertThat(sut.canPlay(state, S7, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, S10, RelPlayer.s)).isFalse();
        assertThat(sut.canPlay(state, SJ, RelPlayer.s)).isTrue();
    }
}