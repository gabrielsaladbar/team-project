package use_case.player_action;

/**
 * Makes a move for the human player.
 */
public interface PlayerActionInputBoundary {
    void execute(PlayerActionInputData inputData);
}
