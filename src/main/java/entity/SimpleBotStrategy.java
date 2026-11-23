package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple bot that scores its hand, then calls, raises or folds based on its
 * personality's thresholds, sometimes raising as a bluff.
 *
 * Hand strength: after the flop it is the HandCategory number (0 = high card,
 * 1 = one pair, 2 = two pair, ...). Before the flop: 3 for a pair of tens or
 * better, 2 for any other pair or two cards ten or higher, 1 for a queen or
 * better, otherwise 0.
 */
public class SimpleBotStrategy implements BotStrategy {

    private static final int TEN_VALUE = 10;
    private static final int QUEEN_VALUE = 12;
    private static final int MAX_RAISE_BIG_BLINDS = 4;

    private final Random random;
    private final HandEvaluator evaluator;

    /**
     * Creates the strategy.
     *
     * @param random used for bluffing; pass a seeded Random to make tests repeatable
     */
    public SimpleBotStrategy(Random random) {
        this.random = random;
        this.evaluator = new HandEvaluator();
    }

    /**
     * Raises with a strong hand (or a bluff), checks when there is nothing to call,
     * calls with a decent hand, and folds otherwise.
     *
     * @param game the current game
     * @param bot the bot that has to act
     * @return a legal decision for the bot
     */
    @Override
    public BotDecision decide(Game game, Player bot) {
        BotPersonality personality = bot.getPersonality();
        if (personality == null) {
            personality = BotPersonality.BALANCED;
        }

        int strength = handStrength(game, bot);
        boolean bluffing = random.nextDouble() < personality.getBluffChance();

        if ((strength >= personality.getRaiseThreshold() || bluffing) && canRaise(game, bot)) {
            return new BotDecision(ActionType.RAISE, game.getMinimumRaiseTo());
        }
        if (game.getAmountToCall(bot) == 0) {
            return new BotDecision(ActionType.CHECK, 0);
        }
        if (strength >= personality.getCallThreshold()) {
            return new BotDecision(ActionType.CALL, 0);
        }
        return new BotDecision(ActionType.FOLD, 0);
    }

    /**
     * A bot can raise if it has enough chips for the minimum raise, and the bet this
     * round is still small (this stops two bots raising each other forever).
     */
    private boolean canRaise(Game game, Player bot) {
        boolean betIsSmall = game.getCurrentHighestBet() < game.getBigBlind() * MAX_RAISE_BIG_BLINDS;
        boolean canAfford = bot.getChips() + bot.getCurrentBet() >= game.getMinimumRaiseTo();
        return betIsSmall && canAfford;
    }

    private int handStrength(Game game, Player bot) {
        List<Card> communityCards = game.getCommunityCards();
        if (communityCards.isEmpty()) {
            return preFlopStrength(bot.getHoleCards());
        }
        List<Card> allCards = new ArrayList<>(bot.getHoleCards());
        allCards.addAll(communityCards);
        return evaluator.evaluate(allCards).getCategory().ordinal();
    }

    private int preFlopStrength(List<Card> holeCards) {
        int first = holeCards.get(0).getRank().getValue();
        int second = holeCards.get(1).getRank().getValue();
        int high = Math.max(first, second);
        int low = Math.min(first, second);

        if (high == low && high >= TEN_VALUE) {
            return 3;
        }
        if (high == low || low >= TEN_VALUE) {
            return 2;
        }
        if (high >= QUEEN_VALUE) {
            return 1;
        }
        return 0;
    }
}
