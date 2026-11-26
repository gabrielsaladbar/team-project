package entity;

/**
 * The kinds of five-card poker hands, listed from weakest to strongest.
 * The order matters: a later category always beats an earlier one.
 */
public enum HandCategory {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    STRAIGHT,
    FLUSH,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    STRAIGHT_FLUSH
}
