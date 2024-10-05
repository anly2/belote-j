package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static io.micronaut.core.util.CollectionUtils.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingInt;
import static me.aanchev.belotej.domain.Card.*;
import static me.aanchev.belotej.domain.PassCall.PASS;
import static me.aanchev.belotej.domain.Team.sameTeam;
import static me.aanchev.belotej.engine.GameState.clear;
import static me.aanchev.utils.DataUtils.pair;

@Service
class GameEngine {

    public RelPlayer nextTrick(GameState state) {
        var winner = state.getTrickWinner();
        state.setTrickInitiator(winner);
        state.setNext(winner);

        int trickPoints = getTrickPoints(state);

        state.getScore().add(trickPoints, winner);

        moveTrickCardsToWinPiles(state, winner);

        return winner;
    }

    public void nextRound(GameState state) {
        var winner = nextTrick(state);
        state.getScore().add(10, winner);

        state.setTrickWinner(null);
        state.setTrickInitiator(null);
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
        var next = state.getTrickInitiator();
        var trick = state.getTrick();
        var winnerCard = trick.get(next);
        var winner = next;
        for (int i = 0; i < 3; i++) {
            next = next.next();
            Card nextCard = trick.get(next);
            if (nextCard == null) break;
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
        var trick = state.getTrick();
        var trump = state.getTrump();
        return getPoints(trick.getW(), trump)
                + getPoints(trick.getN(), trump)
                + getPoints(trick.getE(), trump)
                + getPoints(trick.getS(), trump);
    }


    protected void moveTrickCardsToWinPiles(GameState state, RelPlayer winner) {
        // TODO: Apply the "shuffling strategy" onCollect
        state.getWinPiles().get(winner).addAll(state.getTrick().toList());
        state.getTrick().reset();
    }



    /// Playing ///


    public void startRound(GameState state) {
        if (state.getDeck().isEmpty()) {
            state.getDeck().addAll(state.getInitialDeck());
        }

        var hands = state.getHands();
        if (!(hands.getS().isEmpty()
                && hands.getW().isEmpty()
                && hands.getN().isEmpty()
                && hands.getE().isEmpty()
        )) {
            return;
        }

        dealCards(state, 3);
        dealCards(state, 2);
    }


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
            RelPlayer trickInitiator = state.getDealer().next();
            state.setTrickInitiator(trickInitiator);
            state.setNext(trickInitiator);

            return;
        }


        // It's trick time

        Card card = (Card) action;
        var hand = state.getHands().get(position);
        if (!hand.contains(card)) {
            throw new IllegalStateException("You cannot play the card '" + card + "' because you do not have it in your hand!");
        }

        var trump = state.getTrump();
        var trick = state.getTrick();
        var initiator = state.getTrickInitiator();
        var winner = state.getTrickWinner();
        assertCanPlay(state, card, position);

        state.getCombinations().get(position).addAll(findClaims(hand, card, trump));

        trick.set(position, card);
        hand.remove(card);

        if (winner == null || winsOver(card, trick.get(winner), trump)) {
            state.setTrickWinner(current);
        }

        var next = current.next();
        state.setNext(next);

        if (next == initiator) {
            if (hand.isEmpty() && state.getHands().get(position.next()).isEmpty()) {
                nextRound(state);
                return;
            }

            nextTrick(state);
        }
    }

    protected void assertCanPlay(GameState state, Card card, RelPlayer player) {
        var reason = cannotPlayReason(state, card, player);
        if (reason != null) throw new IllegalArgumentException(reason);
    }
    protected boolean canPlay(GameState state, Card card, RelPlayer player) {
        return cannotPlayReason(state, card, player) == null;
    }
    protected String cannotPlayReason(GameState state, Card card, RelPlayer player) {
        var trick = state.getTrick();
        if (trick.isEmpty()) return null;

        var trump = state.getTrump();
        var initiator = state.getTrickInitiator();
        var winner = state.getTrickWinner();
        var hand = state.getHands().get(player);

        var askedCard = trick.get(initiator);
        var askedSuit = getSuit(askedCard);
        var suit = getSuit(card);

        if (askedSuit == suit) {
            if (Trump.isTrump(askedSuit, trump)) {
                var powerThreshold = getPower(askedCard, trump);
                if (getPower(card, trump) <= powerThreshold) {
                    if (hand.stream().anyMatch(c -> askedSuit == getSuit(c) && getPower(c, trump) > powerThreshold)) {
                        return "You have a card with which to raise but you are not playing it!";
                    }
                }
            }
        } else {
            if (hand.stream().anyMatch(c -> askedSuit == getSuit(c))) {
                return "You have a card from the asked suit but you are not playing it!";
            }
            if (!Trump.isTrump(askedSuit, trump) && !Trump.isTrump(suit, trump)) {
                if (!sameTeam(winner, player)) {
                    if (hand.stream().anyMatch(c -> isTrump(c, trump))) {
                        return "You have a trump but you are not playing it!";
                    }
                }
            }
        }

        return null;
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


    public List<Map.Entry<Claim, List<Card>>> findClaims(List<Card> hand, Card card, Trump trump) {
        var claims = new ArrayList<Map.Entry<Claim, List<Card>>>(0);

        // Find Brelans (4 of a kind) and Runs
        if (hand.size() == 8) {
            var cards = new ArrayList<>(hand);
            cards.sort(comparingInt(GameEngine::cardCombinationValue));

            var values = cards.stream().mapToInt(GameEngine::cardCombinationValue).toArray();

            // Find Brelans
            var counts = new byte[8];
            for (var value : values) {
                counts[value % 10]++;
            }

            for (int i = 0; i < counts.length; i++) {
                if (counts[i] == 4) { // found a Brelan
                    // consume the cards, so they dont make more runs
                    for (int j = 0; j < values.length; j++) {
                        if (values[j] % 10 == i) values[j] = 0;
                    }

                    var _i = i;
                    claims.add(switch (i) {
                        case 2 -> pair(Claim.BRELAN9, List.of(C9, D9, H9, S9));
                        case 4 -> pair(Claim.BRELANJ, List.of(CJ, DJ, HJ, SJ));
                        default -> pair(Claim.BRELAN, cards.stream().filter(c -> cardCombinationValue(c) % 10 == _i).toList());
                    });
                }
            }


            // Find runs
            var run = 0;
            for (int i = 0; i < hand.size(); i++) {
                var v = values[i];

                if ((i + 1) != hand.size() && (v + 1) == values[i + 1]) {
                    run++;
                    continue;
                }
                // TODO: If run is 8 -> TIERCE + QUINT
                if (run >= 3) {
                    claims.add(pair(switch (run) {
                        case 3 -> Claim.TIERCE;
                        case 4 -> Claim.QUARTE;
                        default -> Claim.QUINT;
                    }, new ArrayList<>(cards.subList(i - run + 1, i + 1))));
                }
                run = 0;
            }

        }

        // Find Belote
        var matchingCard = switch (card) {
            case CQ -> CK;
            case DQ -> DK;
            case HQ -> HK;
            case SQ -> SK;
            case CK -> CQ;
            case DK -> DQ;
            case HK -> HQ;
            case SK -> SQ;
            default -> null;
        };
        if (matchingCard != null && isTrump(card, trump)) {
            if (hand.contains(matchingCard)) {
                claims.add(pair(Claim.BELOTE, asList(card, matchingCard)));
            }
        }

        return claims;
    }

    private static final byte[] COMBINATION_VALUES_BY_CARD_ORDINAL = new byte[] {
//          C7, C8, C9, CJ, CQ, CK, C10, CA,
            10, 11, 12, 14, 15, 16, 13,  17,
//          D7, D8, D9, DJ, DQ, DK, D10, DA,
            20, 21, 22, 24, 25, 26, 23,  27,
//          H7, H8, H9, HJ, HQ, HK, H10, HA,
            30, 31, 32, 34, 35, 36, 33,  37,
//          S7, S8, S9, SJ, SQ, SK, S10, SA
            40, 41, 42, 44, 45, 46, 43,  47
    };
    private static int cardCombinationValue(Card card) {
        return COMBINATION_VALUES_BY_CARD_ORDINAL[card.ordinal()];
    }
}
