package use_case;

import entity.ActionType;
import entity.BotDecision;
import entity.BotStrategy;
import entity.Game;
import entity.Player;

/**
 * A predictable bot for tests: it checks when it can and folds to any bet.
 */
public class FoldingBotStrategy implements BotStrategy {

    @Override
    public BotDecision decide(Game game, Player bot) {
        if (game.getAmountToCall(bot) == 0) {
            return new BotDecision(ActionType.CHECK, 0);
        }
        return new BotDecision(ActionType.FOLD, 0);
    }
}
