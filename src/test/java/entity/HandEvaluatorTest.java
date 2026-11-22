package entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HandEvaluatorTest {

    private final HandEvaluator evaluator = new HandEvaluator();

    /**
     * Builds a list of cards from short names separated by spaces, e.g. "As Kd 7h".
     */
    private List<Card> cards(String names) {
        List<Card> result = new ArrayList<>();
        for (String name : names.split(" ")) {
            result.add(new Card(findRank(name.charAt(0)), findSuit(name.charAt(1))));
        }
        return result;
    }

    private Rank findRank(char symbol) {
        for (Rank rank : Rank.values()) {
            if (rank.getSymbol() == symbol) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown rank " + symbol);
    }

    private Suit findSuit(char symbol) {
        for (Suit suit : Suit.values()) {
            if (suit.getSymbol() == symbol) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit " + symbol);
    }

    private HandValue evaluate(String names) {
        return evaluator.evaluate(cards(names));
    }

    // ----- each category is detected -----

    @Test
    void detectsHighCard() {
        HandValue value = evaluate("As Jd 8c 5h 3s");
        assertEquals(HandCategory.HIGH_CARD, value.getCategory());
        assertEquals(Arrays.asList(14, 11, 8, 5, 3), value.getTieBreakers());
    }

    @Test
    void detectsOnePair() {
        HandValue value = evaluate("Ks Kd 9c 5h 3s");
        assertEquals(HandCategory.ONE_PAIR, value.getCategory());
        assertEquals(Arrays.asList(13, 9, 5, 3), value.getTieBreakers());
    }

    @Test
    void detectsTwoPair() {
        HandValue value = evaluate("Ks Kd 5c 5h 3s");
        assertEquals(HandCategory.TWO_PAIR, value.getCategory());
        assertEquals(Arrays.asList(13, 5, 3), value.getTieBreakers());
    }

    @Test
    void detectsThreeOfAKind() {
        HandValue value = evaluate("7s 7d 7c Kh 2s");
        assertEquals(HandCategory.THREE_OF_A_KIND, value.getCategory());
        assertEquals(Arrays.asList(7, 13, 2), value.getTieBreakers());
    }

    @Test
    void detectsStraight() {
        HandValue value = evaluate("9s 8d 7c 6h 5s");
        assertEquals(HandCategory.STRAIGHT, value.getCategory());
        assertEquals(Arrays.asList(9), value.getTieBreakers());
    }

    @Test
    void detectsFlush() {
        HandValue value = evaluate("Ah Jh 8h 4h 2h");
        assertEquals(HandCategory.FLUSH, value.getCategory());
        assertEquals(Arrays.asList(14, 11, 8, 4, 2), value.getTieBreakers());
    }

    @Test
    void detectsFullHouse() {
        HandValue value = evaluate("Qs Qd Qc 4h 4s");
        assertEquals(HandCategory.FULL_HOUSE, value.getCategory());
        assertEquals(Arrays.asList(12, 4), value.getTieBreakers());
    }

    @Test
    void detectsFourOfAKind() {
        HandValue value = evaluate("9s 9d 9c 9h Ks");
        assertEquals(HandCategory.FOUR_OF_A_KIND, value.getCategory());
        assertEquals(Arrays.asList(9, 13), value.getTieBreakers());
    }

    @Test
    void detectsStraightFlush() {
        HandValue value = evaluate("Ts 9s 8s 7s 6s");
        assertEquals(HandCategory.STRAIGHT_FLUSH, value.getCategory());
        assertEquals(Arrays.asList(10), value.getTieBreakers());
    }

    @Test
    void royalFlushIsAceHighStraightFlush() {
        HandValue value = evaluate("As Ks Qs Js Ts");
        assertEquals(HandCategory.STRAIGHT_FLUSH, value.getCategory());
        assertEquals(Arrays.asList(14), value.getTieBreakers());
    }

    // ----- ace-low straight -----

    @Test
    void aceToFiveIsFiveHighStraight() {
        HandValue value = evaluate("As 2d 3c 4h 5s");
        assertEquals(HandCategory.STRAIGHT, value.getCategory());
        assertEquals(Arrays.asList(5), value.getTieBreakers());
    }

    @Test
    void aceToFiveLosesToSixHighStraight() {
        HandValue wheel = evaluate("As 2d 3c 4h 5s");
        HandValue sixHigh = evaluate("2s 3d 4c 5h 6s");
        assertTrue(sixHigh.compareTo(wheel) > 0);
    }

    @Test
    void aceToFiveStraightFlush() {
        HandValue value = evaluate("Ah 2h 3h 4h 5h");
        assertEquals(HandCategory.STRAIGHT_FLUSH, value.getCategory());
        assertEquals(Arrays.asList(5), value.getTieBreakers());
    }

    @Test
    void straightDoesNotWrapAroundAce() {
        HandValue value = evaluate("Qs Kd Ac 2h 3s");
        assertEquals(HandCategory.HIGH_CARD, value.getCategory());
    }

    // ----- ordering between categories -----

    @Test
    void everyCategoryBeatsTheOneBelowIt() {
        HandValue[] ordered = {
            evaluate("As Jd 8c 5h 3s"),
            evaluate("2s 2d 5c 7h 9s"),
            evaluate("2s 2d 3c 3h 5s"),
            evaluate("2s 2d 2c 4h 5s"),
            evaluate("As 2d 3c 4h 5s"),
            evaluate("2h 4h 6h 8h Th"),
            evaluate("2s 2d 2c 3h 3s"),
            evaluate("2s 2d 2c 2h 3s"),
            evaluate("Ah 2h 3h 4h 5h")
        };
        for (int i = 1; i < ordered.length; i++) {
            assertTrue(ordered[i].compareTo(ordered[i - 1]) > 0,
                    ordered[i] + " should beat " + ordered[i - 1]);
        }
    }

    // ----- tie-breaks -----

    @Test
    void kickerBreaksPairTie() {
        HandValue kingsWithAce = evaluate("Ks Kd Ac 7h 3s");
        HandValue kingsWithQueen = evaluate("Kh Kc Qc 7d 3c");
        assertTrue(kingsWithAce.compareTo(kingsWithQueen) > 0);
        assertTrue(kingsWithQueen.compareTo(kingsWithAce) < 0);
    }

    @Test
    void lastKickerCanDecideHighCard() {
        HandValue better = evaluate("As Jd 8c 5h 3s");
        HandValue worse = evaluate("Ah Jc 8d 5s 2c");
        assertTrue(better.compareTo(worse) > 0);
    }

    @Test
    void twoPairComparesHighPairFirst() {
        HandValue acesAndTwos = evaluate("As Ad 2c 2h 3s");
        HandValue kingsAndQueens = evaluate("Ks Kd Qc Qh Js");
        assertTrue(acesAndTwos.compareTo(kingsAndQueens) > 0);
    }

    @Test
    void twoPairComparesLowPairNext() {
        HandValue kingsAndNines = evaluate("Ks Kd 9c 9h 2s");
        HandValue kingsAndEights = evaluate("Kh Kc 8c 8h As");
        assertTrue(kingsAndNines.compareTo(kingsAndEights) > 0);
    }

    @Test
    void twoPairComparesKickerLast() {
        HandValue withAce = evaluate("Ks Kd 9c 9h As");
        HandValue withQueen = evaluate("Kh Kc 9d 9s Qs");
        assertTrue(withAce.compareTo(withQueen) > 0);
    }

    @Test
    void fullHouseComparesThreeOfAKindFirst() {
        HandValue threesFullOfAces = evaluate("3s 3d 3c Ah As");
        HandValue twosFullOfKings = evaluate("2s 2d 2c Kh Ks");
        assertTrue(threesFullOfAces.compareTo(twosFullOfKings) > 0);
    }

    @Test
    void fullHouseComparesPairNext() {
        HandValue queensFullOfNines = evaluate("Qs Qd Qc 9h 9s");
        HandValue queensFullOfFives = evaluate("Qs Qd Qh 5h 5s");
        assertTrue(queensFullOfNines.compareTo(queensFullOfFives) > 0);
    }

    // ----- best 5 of 7 -----

    @Test
    void picksFlushOverStraightFromSevenCards() {
        HandValue value = evaluate("9h 8d 7h 6h 5c 2h Kh");
        assertEquals(HandCategory.FLUSH, value.getCategory());
        assertEquals(Arrays.asList(13, 9, 7, 6, 2), value.getTieBreakers());
    }

    @Test
    void picksHighestStraightFromSevenCards() {
        HandValue value = evaluate("4s 5d 6c 7h 8s 9d 2c");
        assertEquals(HandCategory.STRAIGHT, value.getCategory());
        assertEquals(Arrays.asList(9), value.getTieBreakers());
    }

    @Test
    void picksBestTwoPairsAndKickerFromThreePairs() {
        HandValue value = evaluate("Ks Kd 9c 9h 4s 4d 2c");
        assertEquals(HandCategory.TWO_PAIR, value.getCategory());
        assertEquals(Arrays.asList(13, 9, 4), value.getTieBreakers());
    }

    @Test
    void twoThreeOfAKindsMakeFullHouse() {
        HandValue value = evaluate("8s 8d 8c 5h 5s 5d Ac");
        assertEquals(HandCategory.FULL_HOUSE, value.getCategory());
        assertEquals(Arrays.asList(8, 5), value.getTieBreakers());
    }

    @Test
    void fullHouseUsesBestAvailablePair() {
        HandValue value = evaluate("8s 8d 8c 5h 5s Jd Jc");
        assertEquals(HandCategory.FULL_HOUSE, value.getCategory());
        assertEquals(Arrays.asList(8, 11), value.getTieBreakers());
    }

    @Test
    void fourOfAKindUsesBestKickerFromSevenCards() {
        HandValue value = evaluate("6s 6d 6c 6h Qs Qd 3c");
        assertEquals(HandCategory.FOUR_OF_A_KIND, value.getCategory());
        assertEquals(Arrays.asList(6, 12), value.getTieBreakers());
    }

    @Test
    void picksStraightFlushInsideLargerFlush() {
        HandValue value = evaluate("2s 5s 6s 7s 8s 9s Ad");
        assertEquals(HandCategory.STRAIGHT_FLUSH, value.getCategory());
        assertEquals(Arrays.asList(9), value.getTieBreakers());
    }

    @Test
    void worksWithSixCards() {
        HandValue value = evaluate("Ts Td 4c 4h 4s 2d");
        assertEquals(HandCategory.FULL_HOUSE, value.getCategory());
        assertEquals(Arrays.asList(4, 10), value.getTieBreakers());
    }

    // ----- equal hands -----

    @Test
    void identicalHandsInDifferentSuitsCompareEqual() {
        HandValue first = evaluate("As Kd 9c 7h 3s");
        HandValue second = evaluate("Ah Kc 9d 7s 3c");
        assertEquals(0, first.compareTo(second));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void boardPlaysForBothPlayers() {
        String board = "As Ks Qd Jc Th";
        HandValue first = evaluate(board + " 2c 3d");
        HandValue second = evaluate(board + " 4h 5h");
        assertEquals(0, first.compareTo(second));
    }

    // ----- invalid input -----

    @Test
    void rejectsFewerThanFiveCards() {
        try {
            evaluate("As Kd 9c 7h");
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    void rejectsMoreThanSevenCards() {
        try {
            evaluate("As Kd 9c 7h 3s 2d 4c 5h");
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
