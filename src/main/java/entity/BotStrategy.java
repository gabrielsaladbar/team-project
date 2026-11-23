package entity;

/**
 * Decides what a bot does on its turn. Different strategies (for example a
 * smarter or API-based one) can be swapped in without changing the use cases.
 */
public interface BotStrategy {

    /**
     * Chooses a move for the bot whose turn it is.
     *
     * @param game the current game
     * @param bot the bot that has to act
     * @return the chosen action and, for a raise, the total amount to raise to
     */
    BotDecision decide(Game game, Player bot);
}
