package entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class DeckTest {

    @Test
    void newDeckHas52UniqueCards() {
        Deck deck = new Deck();
        assertEquals(52, deck.cardsRemaining());

        Set<Card> seen = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            seen.add(deck.deal());
        }
        assertEquals(52, seen.size());
    }

    @Test
    void dealingReducesCount() {
        Deck deck = new Deck();
        deck.deal();
        assertEquals(51, deck.cardsRemaining());
        deck.deal();
        assertEquals(50, deck.cardsRemaining());
    }

    @Test
    void shuffledDeckStillHas52UniqueCards() {
        Deck deck = new Deck(new Random(7));
        deck.shuffle();
        Set<Card> seen = new HashSet<>();
        while (deck.cardsRemaining() > 0) {
            seen.add(deck.deal());
        }
        assertEquals(52, seen.size());
    }

    @Test
    void sameSeedGivesSameOrder() {
        Deck first = new Deck(new Random(42));
        Deck second = new Deck(new Random(42));
        first.shuffle();
        second.shuffle();

        List<Card> firstCards = new ArrayList<>();
        List<Card> secondCards = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            firstCards.add(first.deal());
            secondCards.add(second.deal());
        }
        assertEquals(firstCards, secondCards);
    }

    @Test
    void dealingFromEmptyDeckThrows() {
        Deck deck = new Deck();
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        try {
            deck.deal();
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}
