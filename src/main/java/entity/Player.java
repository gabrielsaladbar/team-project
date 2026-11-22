package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A player at the poker table, either the human or a bot. Tracks the player's
 * chips, hole cards, bet in the current betting round, and whether they folded.
 */
public class Player {

    private final String name;
    private final boolean isBot;
    private final List<Card> holeCards;
    private int chips;
    private int currentBet;
    private boolean folded;

    /**
     * Creates a player with no cards and no bet.
     *
     * @param name the name shown for this player
     * @param chips the number of chips the player starts with
     * @param isBot true if this player is controlled by the computer
     */
    public Player(String name, int chips, boolean isBot) {
        this.name = name;
        this.chips = chips;
        this.isBot = isBot;
        this.holeCards = new ArrayList<>();
        this.currentBet = 0;
        this.folded = false;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isFolded() {
        return folded;
    }

    public boolean isBot() {
        return isBot;
    }

    /**
     * Returns a copy of this player's hole cards, so callers cannot change them.
     *
     * @return the player's hole cards (empty before cards are dealt)
     */
    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }

    /**
     * Gives this player their two hole cards for the hand.
     *
     * @param first the first hole card
     * @param second the second hole card
     */
    public void receiveCards(Card first, Card second) {
        holeCards.clear();
        holeCards.add(first);
        holeCards.add(second);
    }

    /**
     * Moves chips from this player's stack into their bet for the current round.
     *
     * @param amount the number of chips to bet
     * @throws IllegalArgumentException if amount is negative or more than the player has
     */
    public void bet(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bet amount cannot be negative.");
        }
        if (amount > chips) {
            throw new IllegalArgumentException(
                    name + " cannot bet " + amount + " with only " + chips + " chips.");
        }
        chips = chips - amount;
        currentBet = currentBet + amount;
    }

    /**
     * Marks this player as folded for the rest of the hand.
     */
    public void fold() {
        folded = true;
    }

    /**
     * Clears the hole cards, bet and folded flag so the player is ready for a new hand.
     * The player's chip count is kept.
     */
    public void resetForNewHand() {
        holeCards.clear();
        currentBet = 0;
        folded = false;
    }
}
