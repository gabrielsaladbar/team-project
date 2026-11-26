package use_case.start_hand;

import entity.ActionType;
import entity.Game;
import use_case.GameStateOutputData;

import java.util.List;

/**
 * The table after a hand has been dealt and the bots before the human have acted.
 */
public class StartHandOutputData extends GameStateOutputData {

    public StartHandOutputData(Game game, List<ActionType> humanLegalActions) {
        super(game, humanLegalActions);
    }
}
