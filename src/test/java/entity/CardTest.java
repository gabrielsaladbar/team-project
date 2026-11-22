package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CardTest {

    @Test
    void toStringUsesShortName() {
        assertEquals("As", new Card(Rank.ACE, Suit.SPADES).toString());
        assertEquals("Td", new Card(Rank.TEN, Suit.DIAMONDS).toString());
        assertEquals("2c", new Card(Rank.TWO, Suit.CLUBS).toString());
    }

    @Test
    void cardsWithSameRankAndSuitAreEqual() {
        Card first = new Card(Rank.KING, Suit.HEARTS);
        Card second = new Card(Rank.KING, Suit.HEARTS);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void cardsWithDifferentSuitAreNotEqual() {
        assertNotEquals(new Card(Rank.KING, Suit.HEARTS), new Card(Rank.KING, Suit.SPADES));
    }

    @Test
    void rankValuesGoFromTwoToFourteen() {
        assertEquals(2, Rank.TWO.getValue());
        assertEquals(10, Rank.TEN.getValue());
        assertEquals(14, Rank.ACE.getValue());
    }
}
