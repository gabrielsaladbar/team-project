package entity;

/**
 * A single playing card with a rank and a suit. Cards are immutable.
 */
public class Card {

    private final Rank rank;
    private final Suit suit;

    /**
     * Creates a card with the given rank and suit.
     *
     * @param rank the rank of the card
     * @param suit the suit of the card
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Card)) {
            return false;
        }
        Card otherCard = (Card) other;
        return rank == otherCard.rank && suit == otherCard.suit;
    }

    @Override
    public int hashCode() {
        return rank.hashCode() * 31 + suit.hashCode();
    }

    /**
     * Returns a short name for the card, such as "As" for the ace of spades
     * or "Td" for the ten of diamonds.
     *
     * @return the rank symbol followed by the suit symbol
     */
    @Override
    public String toString() {
        return "" + rank.getSymbol() + suit.getSymbol();
    }
}
