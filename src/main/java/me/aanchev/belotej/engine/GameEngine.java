package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.micronaut.core.util.CollectionUtils.concat;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingInt;
import static me.aanchev.belotej.domain.Card.*;
import static me.aanchev.belotej.domain.PassCall.PASS;
import static me.aanchev.belotej.engine.GameState.clear;

@Service
class GameEngine {

    public void nextTrick(GameState state) {
        if (state.getTrick().isEmpty()) return;

        RelPlayer winner = getTrickWinner(state);
        state.setNext(winner);

        int trickPoints = getTrickPoints(state);

        state.getScore().add(trickPoints, winner);

        moveTrickCardsToWinPiles(state, winner);
    }

    public void nextRound(GameState state) {
        nextTrick(state);

        state.setDealer(state.getDealer().next()); // shift the dealer to the next
        state.setNext(state.getDealer().next());   // shift the next to the next of the dealer (yet again)
        updateGameScore(state);
    }

    protected void updateGameScore(GameState state) {
        Scores score = state.getScore();
        int pointsUs = roundScoreToGameScore(score.getUs(), state.getTrump(), score.getThem());
        int pointsThem = roundScoreToGameScore(score.getThem(), state.getTrump(), score.getUs());

        // Add bonus for Capot
        if (pointsUs == 0) pointsThem += 9;
        if (pointsThem == 0) pointsUs += 9;

        // Count declarations
        Scores declarationMatchPoints = computeDeclarationMatchPoints(state);
        pointsUs += declarationMatchPoints.getUs();
        pointsThem += declarationMatchPoints.getThem();

        // TODO: Whenever "Contra" and "Recontra" are impl, double/quadruple here

        if (state.getChallengers() == Team.US) {
            if (pointsUs < pointsThem) {
                pointsThem += pointsUs;
                pointsUs = 0;
            }
        }
        if (state.getChallengers() == Team.THEM) {
            if (pointsThem < pointsUs) {
                pointsUs += pointsThem;
                pointsThem = 0;
            }
        }
        state.getGameScore().add(pointsUs, pointsThem);
        state.getScore().reset();
        clear(state.getCombinations());
    }

    public int roundScoreToGameScore(int points, Trump trump, int otherPoints) {
        int p = points / 10;
        int r = points % 10;
        int roundingLimit = switch (trump) {
            case C, D, H, S -> 6;
            case A -> 5;
            case J -> 4;
        };
        if (r > roundingLimit) p++;
        if (r == roundingLimit && points < otherPoints) p++;
        if (trump == Trump.A) p *= 2;
        return p;
    }

    protected Scores computeDeclarationMatchPoints(GameState state) {
        var claims = state.getCombinations();
        var claimsUs = concat(claims.getN(), claims.getS());
        claimsUs.sort(CLAIMS_COMPARATOR);
        var claimsThem = concat(claims.getW(), claims.getE());
        claimsThem.sort(CLAIMS_COMPARATOR);

        var strongestUs = claimsUs.isEmpty() || claimsUs.get(0).getKey() == Claim.BELOTE ? null : claimsUs.get(0);
        var strongestThem = claimsThem.isEmpty() || claimsThem.get(0).getKey() == Claim.BELOTE ? null : claimsThem.get(0);
        int cmp = CLAIMS_COMPARATOR.compare(strongestUs, strongestThem);
        if (cmp == 0) {
            boolean strongestUsTrump = strongestUs != null && isTrump(strongestUs.getValue().get(0), state.getTrump());
            boolean strongestThemTrump = strongestThem != null && isTrump(strongestThem.getValue().get(0), state.getTrump());
            if (strongestUsTrump && !strongestThemTrump) cmp--;
            if (strongestThemTrump && !strongestUsTrump) cmp++;
        }
        if (cmp <= 0) claimsThem.clear();
        if (cmp >= 0) claimsUs.clear();

        return new Scores(
                computeDeclarationMatchPoints(claimsUs),
                computeDeclarationMatchPoints(claimsThem)
        );
    }

    public static final Comparator<Map.Entry<Claim, List<Card>>> CLAIMS_COMPARATOR = comparingInt(claim ->
        - (claim == null ? -1 : switch (claim.getKey()) {
            case BELOTE -> -1;
            case TIERCE -> 10;
            case QUARTE -> 20;
            case QUINT -> 30;
            case BRELAN -> 40;
            case BRELAN9 -> 50;
            case BRELANJ -> 60;
        } + getPower(claim.getValue().get(0), Trump.A))
    );

    protected int computeDeclarationMatchPoints(List<Map.Entry<Claim, List<Card>>> declarations) {
        return declarations.stream().mapToInt(declaration -> Claim.getPoints(declaration.getKey())).sum();
    }


    public RelPlayer getTrickWinner(GameState state) {
        var next = state.getNext(); // and initial
        var trick = state.getTrick();
        var winnerCard = trick.get(next);
        var winner = next;
        for (int i = 0; i < 3; i++) {
            next = next.next();
            Card nextCard = trick.get(next);
            if (winsOver(nextCard, winnerCard, state.getTrump())) {
                winner = next;
                winnerCard = nextCard;
            }
        }
        return winner;
    }

    public boolean winsOver(Card a, Card b, Trump trump) {
        Trump suitA = getSuit(a);
        Trump suitB = getSuit(b);
        if (suitA != suitB) {
            if (Trump.isTrump(suitB, trump)) return false; // cant beat a trump of a different suite, even if a trump itself
            if (Trump.isTrump(suitA, trump)) return true; // A is a trump whilst B is not
            return false;
        }
        // same suit semantics
        return getPower(a, trump) > getPower(b, trump);
    }


    public int getTrickPoints(GameState state) {
        // Simply count
        WNES<Card> trick = state.getTrick();
        Trump trump = state.getTrump();
        int points = getPoints(trick.getW(), trump)
                + getPoints(trick.getN(), trump)
                + getPoints(trick.getE(), trump)
                + getPoints(trick.getS(), trump);

        // Add 10 if is the last trick of the round
        WNES<List<Card>> hands = state.getHands();
        if (hands.getW().isEmpty() && hands.getN().isEmpty() && hands.getE().isEmpty() && hands.getS().isEmpty()) {
            points += 10;
        }

        return points;
    }


    protected void moveTrickCardsToWinPiles(GameState state, RelPlayer winner) {
        // TODO: Apply the "shuffling strategy" onCollect
        state.getWinPiles().get(winner).addAll(state.getTrick().toList());
        state.getTrick().reset();
    }



    /// Playing ///


    public void play(GameState state, RelPlayer position, GameAction action) {
        RelPlayer current = state.getNext();
        if (current != position) throw new IllegalStateException("Not your turn!");

        // If it's bidding time
        if (state.getTrump() == null) {
            if (!(action instanceof TrumpCall)) throw new IllegalArgumentException("Can only make bids in the bidding phase!");
            var winningCall = state.getWinningCall();
            if (action != PASS) {
                // Check if going up
                if (BID_COMPARATOR.compare(winningCall, (TrumpCall) action) <= 0) {
                    throw new IllegalArgumentException("Cannot place weaker bids. Already at: " + winningCall);
                }
            }

            state.getCalls().get(position).add((TrumpCall) action);
            var next = current.next();
            state.setNext(next);
            if (action != PASS) {
                state.setWinningCall((TrumpCall) action);
                state.setChallengers(Team.of(position));
            }

            // If roundabout is complete
            if (state.getCalls().get(next).getLast() != winningCall) return;

            if (winningCall == PASS) { // everyone passed
                foldRound(state);
                return;
            }

            state.setTrump((Trump) winningCall);
            state.setWinningCall(null);
            dealCards(state, 3);
            state.setNext(state.getDealer().next());

            return;
        }

        // It's trick time

    }

    public static final Comparator<TrumpCall> BID_COMPARATOR = comparingInt(bid ->
            - (bid == null || bid == PASS ? -1 : ((Trump) bid).ordinal())
    );


    public void foldRound(GameState state) {
        moveHandCardsToDeck(state);
        clear(state.getCalls());
        state.setWinningCall(null);
        state.setChallengers(null);
        state.setTrump(null);

        state.setDealer(state.getDealer().next());
        state.setNext(state.getDealer().next());
        dealCards(state, 3);
        dealCards(state, 2);
    }

    protected void moveHandCardsToDeck(GameState state) {
        // TODO: Apply the "shuffling strategy" onAllPass
        var hands = state.getHands().toList();
        shuffle(hands, state.getRandomSeed());
        hands.forEach(state.getDeck()::addAll);

        cutDeck(state, state.getRandomSeed().nextInt(state.getDeck().size()));

        clear(state.getHands());
    }


    public void cutDeck(GameState state, int pivot) {
        var deck = state.getDeck();
        var copy = new ArrayList<>(deck);
        deck.clear();
        deck.addAll(copy.subList(0, pivot));
        deck.addAll(copy.subList(pivot, copy.size()));
    }

    public void dealCards(GameState state, int amount) {
        var next = state.getDealer().next();
        for (int i = 0; i < 4; i++) {
            var cards = takeFromDeck(state, amount);
            state.getHands().get(next).addAll(cards);
            next = next.next();
        }
    }

    public List<Card> takeFromDeck(GameState state, int amount) {
        var taken = state.getDeck().subList(state.getDeck().size() - amount, state.getDeck().size());
        var copy = new ArrayList<>(taken);
        taken.clear();
        return copy;
    }
}
