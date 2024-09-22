package me.aanchev.belotej.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerState {
    private RelPlayer dealer;
    private RelPlayer playerInTurn;

    private List<Card> hand;

    private WNES<List<TrumpCall>> calls;
    private Trump trump;
    private Team challengers;

    private WNES<Card> trick;
    private WNES<List<Claim>> claims;

    private Scores score;
    private Scores gameScore;
}

