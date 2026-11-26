package use_case.player_action;

import entity.ActionType;

/**
 * The move the human wants to make.
 */
public class PlayerActionInputData {

    private final ActionType actionType;
    private final int raiseAmount;

    /**
     * Creates the input.
     *
     * @param actionType the move (fold, check, call or raise)
     * @param raiseAmount for a raise, the total bet to raise to this round; ignored otherwise
     */
    public PlayerActionInputData(ActionType actionType, int raiseAmount) {
        this.actionType = actionType;
        this.raiseAmount = raiseAmount;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getRaiseAmount() {
        return raiseAmount;
    }
}
