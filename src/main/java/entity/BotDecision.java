package entity;

/**
 * The move a bot has chosen. For a RAISE, the amount is the total bet the bot
 * raises to this round; for other actions it is 0.
 */
public class BotDecision {

    private final ActionType type;
    private final int amount;

    /**
     * Creates a decision.
     *
     * @param type the action chosen
     * @param amount the total bet to raise to, or 0 if the action is not a raise
     */
    public BotDecision(ActionType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public ActionType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
