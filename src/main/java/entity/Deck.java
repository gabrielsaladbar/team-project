package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A standard 52-card deck that can be shuffled and dealt from.
 */
public class Deck {

    private final List<Card> cards;
    private final Random random;

    /**
     * Creates a full, unshuffled deck that uses a new Random for shuffling.
     */
    public Deck() {
        this(new Random());
    }

    /**
     * Creates a full, unshuffled deck that uses the given Random for shuffling.
     * Passing a Random with a fixed seed makes the shuffle order repeatable in tests.
     *
     * @param random the random number generator used by shuffle()
     */
    public Deck(Random random) {
        this.random = random;
        this.cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    /**
     * Randomly reorders the cards left in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    /**
     * Removes and returns the top card of the deck.
     *
     * @return the top card
     * @throws IllegalStateException if the deck is empty
     */
    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from an empty deck.");
        }
        return cards.remove(cards.size() - 1);
    }

    public int cardsRemaining() {
        return cards.size();
    }
}
