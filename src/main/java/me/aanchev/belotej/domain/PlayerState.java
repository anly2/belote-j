package me.aanchev.belotej.domain;

import lombok.Data;

import java.util.List;

@Data
public class PlayerState {
    private RelPlayer dealer;

    private List<Card> hand;

    private WNES<List<Trump>> calls;
    private Trump trump;
    private Team challengers;

    private WNES<Card> trick;
    private WNES<List<Claim>> claims;

    private Scores score;
    private Scores gameScore;
}

