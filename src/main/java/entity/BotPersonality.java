package entity;

/**
 * How a bot plays. Each personality has a minimum hand strength needed to call
 * or raise, and a chance of raising anyway as a bluff.
 * Hand strength goes from 0 (nothing) upward; see SimpleBotStrategy.
 */
public enum BotPersonality {
    CAUTIOUS(1, 3, 0.0),
    BALANCED(1, 2, 0.1),
    AGGRESSIVE(0, 2, 0.25);

    private final int callThreshold;
    private final int raiseThreshold;
    private final double bluffChance;

    BotPersonality(int callThreshold, int raiseThreshold, double bluffChance) {
        this.callThreshold = callThreshold;
        this.raiseThreshold = raiseThreshold;
        this.bluffChance = bluffChance;
    }

    public int getCallThreshold() {
        return callThreshold;
    }

    public int getRaiseThreshold() {
        return raiseThreshold;
    }

    public double getBluffChance() {
        return bluffChance;
    }
}
