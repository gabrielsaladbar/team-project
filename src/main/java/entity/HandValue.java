package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The strength of a five-card poker hand: its category plus the rank values
 * used to break ties between two hands of the same category.
 * A stronger hand compares greater than a weaker one.
 */
public class HandValue implements Comparable<HandValue> {

    private final HandCategory category;
    private final List<Integer> tieBreakers;

    /**
     * Creates a hand value.
     *
     * @param category the kind of hand
     * @param tieBreakers rank values (2 to 14) in the order they should be compared,
     *                    most important first; for example a pair of kings with
     *                    A, 9, 4 kickers is [13, 14, 9, 4]
     */
    public HandValue(HandCategory category, List<Integer> tieBreakers) {
        this.category = category;
        this.tieBreakers = new ArrayList<>(tieBreakers);
    }

    public HandCategory getCategory() {
        return category;
    }

    public List<Integer> getTieBreakers() {
        return new ArrayList<>(tieBreakers);
    }

    /**
     * Compares the category first, then each tie-breaker in order.
     *
     * @param other the hand to compare against
     * @return a positive number if this hand is stronger, a negative number if it is
     *         weaker, or 0 if the two hands are worth the same
     */
    @Override
    public int compareTo(HandValue other) {
        if (category != other.category) {
            return category.compareTo(other.category);
        }
        for (int i = 0; i < tieBreakers.size() && i < other.tieBreakers.size(); i++) {
            int mine = tieBreakers.get(i);
            int theirs = other.tieBreakers.get(i);
            if (mine != theirs) {
                return mine - theirs;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HandValue)) {
            return false;
        }
        HandValue otherValue = (HandValue) other;
        return category == otherValue.category && tieBreakers.equals(otherValue.tieBreakers);
    }

    @Override
    public int hashCode() {
        return category.hashCode() * 31 + tieBreakers.hashCode();
    }

    @Override
    public String toString() {
        return category + " " + tieBreakers;
    }
}
