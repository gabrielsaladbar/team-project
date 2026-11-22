package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Works out the best five-card poker hand that can be made from 5 to 7 cards
 * (for example a player's two hole cards plus the community cards).
 */
public class HandEvaluator {

    private static final int MIN_CARDS = 5;
    private static final int MAX_CARDS = 7;
    private static final int HAND_SIZE = 5;
    private static final int ACE_VALUE = 14;
    private static final int LOWEST_STRAIGHT_HIGH = 5;

    /**
     * Returns the value of the best five-card hand that can be made from the given cards.
     * Categories are checked from strongest to weakest, and the first one found is returned.
     *
     * @param cards between 5 and 7 cards
     * @return the value of the best hand
     * @throws IllegalArgumentException if there are fewer than 5 or more than 7 cards
     */
    public HandValue evaluate(List<Card> cards) {
        if (cards.size() < MIN_CARDS || cards.size() > MAX_CARDS) {
            throw new IllegalArgumentException("Need between 5 and 7 cards, got " + cards.size());
        }

        Map<Rank, Integer> rankCounts = countRanks(cards);
        List<Card> flushCards = findFlushCards(cards);
        List<Rank> quads = ranksWithCount(rankCounts, 4);
        List<Rank> trips = ranksWithCount(rankCounts, 3);
        List<Rank> pairs = ranksWithCount(rankCounts, 2);

        // Straight flush: five cards in a row, all in the flush suit.
        if (flushCards != null) {
            int straightFlushHigh = findStraightHigh(flushCards);
            if (straightFlushHigh > 0) {
                return makeValue(HandCategory.STRAIGHT_FLUSH, straightFlushHigh);
            }
        }

        // Four of a kind, plus the best remaining card as a kicker.
        if (!quads.isEmpty()) {
            Rank quadRank = quads.get(0);
            List<Integer> tieBreakers = new ArrayList<>();
            tieBreakers.add(quadRank.getValue());
            tieBreakers.addAll(findKickers(rankCounts, quadRank, null, 1));
            return new HandValue(HandCategory.FOUR_OF_A_KIND, tieBreakers);
        }

        // Full house: the best three of a kind, plus the best other pair
        // (a second three of a kind also counts as the pair).
        if (!trips.isEmpty()) {
            Rank threeRank = trips.get(0);
            Rank pairRank = null;
            if (trips.size() > 1) {
                pairRank = trips.get(1);
            }
            if (!pairs.isEmpty() && (pairRank == null || pairs.get(0).getValue() > pairRank.getValue())) {
                pairRank = pairs.get(0);
            }
            if (pairRank != null) {
                List<Integer> tieBreakers = new ArrayList<>();
                tieBreakers.add(threeRank.getValue());
                tieBreakers.add(pairRank.getValue());
                return new HandValue(HandCategory.FULL_HOUSE, tieBreakers);
            }
        }

        // Flush: the five highest cards of the flush suit.
        if (flushCards != null) {
            Map<Rank, Integer> flushCounts = countRanks(flushCards);
            return new HandValue(HandCategory.FLUSH, findKickers(flushCounts, null, null, HAND_SIZE));
        }

        // Straight: five ranks in a row, any suits.
        int straightHigh = findStraightHigh(cards);
        if (straightHigh > 0) {
            return makeValue(HandCategory.STRAIGHT, straightHigh);
        }

        // Three of a kind, plus the two best other cards.
        if (!trips.isEmpty()) {
            Rank threeRank = trips.get(0);
            List<Integer> tieBreakers = new ArrayList<>();
            tieBreakers.add(threeRank.getValue());
            tieBreakers.addAll(findKickers(rankCounts, threeRank, null, 2));
            return new HandValue(HandCategory.THREE_OF_A_KIND, tieBreakers);
        }

        // Two pair: the two best pairs, plus the best other card.
        if (pairs.size() >= 2) {
            Rank highPair = pairs.get(0);
            Rank lowPair = pairs.get(1);
            List<Integer> tieBreakers = new ArrayList<>();
            tieBreakers.add(highPair.getValue());
            tieBreakers.add(lowPair.getValue());
            tieBreakers.addAll(findKickers(rankCounts, highPair, lowPair, 1));
            return new HandValue(HandCategory.TWO_PAIR, tieBreakers);
        }

        // One pair, plus the three best other cards.
        if (pairs.size() == 1) {
            Rank pairRank = pairs.get(0);
            List<Integer> tieBreakers = new ArrayList<>();
            tieBreakers.add(pairRank.getValue());
            tieBreakers.addAll(findKickers(rankCounts, pairRank, null, 3));
            return new HandValue(HandCategory.ONE_PAIR, tieBreakers);
        }

        // High card: the five best cards.
        return new HandValue(HandCategory.HIGH_CARD, findKickers(rankCounts, null, null, HAND_SIZE));
    }

    /**
     * Counts how many cards there are of each rank.
     */
    private Map<Rank, Integer> countRanks(List<Card> cards) {
        Map<Rank, Integer> counts = new HashMap<>();
        for (Card card : cards) {
            Rank rank = card.getRank();
            if (counts.containsKey(rank)) {
                counts.put(rank, counts.get(rank) + 1);
            } else {
                counts.put(rank, 1);
            }
        }
        return counts;
    }

    /**
     * Returns the cards of the suit that has at least five cards, or null if there is no flush.
     * With at most 7 cards, only one suit can have five or more.
     */
    private List<Card> findFlushCards(List<Card> cards) {
        Map<Suit, List<Card>> cardsBySuit = new HashMap<>();
        for (Card card : cards) {
            Suit suit = card.getSuit();
            if (!cardsBySuit.containsKey(suit)) {
                cardsBySuit.put(suit, new ArrayList<Card>());
            }
            cardsBySuit.get(suit).add(card);
        }
        for (Suit suit : Suit.values()) {
            List<Card> suitCards = cardsBySuit.get(suit);
            if (suitCards != null && suitCards.size() >= HAND_SIZE) {
                return suitCards;
            }
        }
        return null;
    }

    /**
     * Returns the ranks that appear exactly the given number of times, highest rank first.
     */
    private List<Rank> ranksWithCount(Map<Rank, Integer> rankCounts, int count) {
        List<Rank> result = new ArrayList<>();
        Rank[] allRanks = Rank.values();
        for (int i = allRanks.length - 1; i >= 0; i--) {
            Integer rankCount = rankCounts.get(allRanks[i]);
            if (rankCount != null && rankCount == count) {
                result.add(allRanks[i]);
            }
        }
        return result;
    }

    /**
     * Returns the values of the highest ranks present, skipping up to two excluded ranks.
     * Each rank is used at most once, so this gives the kickers for a hand.
     *
     * @param rankCounts how many cards of each rank are available
     * @param excludedOne a rank to skip, or null
     * @param excludedTwo another rank to skip, or null
     * @param howMany how many values to return
     */
    private List<Integer> findKickers(Map<Rank, Integer> rankCounts, Rank excludedOne,
                                      Rank excludedTwo, int howMany) {
        List<Integer> kickers = new ArrayList<>();
        Rank[] allRanks = Rank.values();
        for (int i = allRanks.length - 1; i >= 0 && kickers.size() < howMany; i--) {
            Rank rank = allRanks[i];
            if (rank != excludedOne && rank != excludedTwo && rankCounts.containsKey(rank)) {
                kickers.add(rank.getValue());
            }
        }
        return kickers;
    }

    /**
     * Returns the value of the highest card of the best straight in the cards, or 0 if
     * there is no straight. The ace can also be used low, so A-2-3-4-5 is a straight
     * with a high card of 5.
     */
    private int findStraightHigh(List<Card> cards) {
        for (int high = ACE_VALUE; high >= LOWEST_STRAIGHT_HIGH; high--) {
            boolean isStraight = true;
            for (int value = high; value > high - HAND_SIZE; value--) {
                int neededValue = value;
                if (neededValue == 1) {
                    neededValue = ACE_VALUE;
                }
                if (!hasRankValue(cards, neededValue)) {
                    isStraight = false;
                }
            }
            if (isStraight) {
                return high;
            }
        }
        return 0;
    }

    private boolean hasRankValue(List<Card> cards, int value) {
        for (Card card : cards) {
            if (card.getRank().getValue() == value) {
                return true;
            }
        }
        return false;
    }

    private HandValue makeValue(HandCategory category, int highValue) {
        List<Integer> tieBreakers = new ArrayList<>();
        tieBreakers.add(highValue);
        return new HandValue(category, tieBreakers);
    }
}
