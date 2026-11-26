package use_case.player_action;

import entity.ActionType;
import entity.Game;
import use_case.GameStateOutputData;

import java.util.List;

/**
 * The table after the human's move and the bots' moves that followed it.
 */
public class PlayerActionOutputData extends GameStateOutputData {

    public PlayerActionOutputData(Game game, List<ActionType> humanLegalActions) {
        super(game, humanLegalActions);
    }
}
