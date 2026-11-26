package use_case.start_hand;

/**
 * Starts a new game, or deals the next hand of the current game.
 */
public interface StartHandInputBoundary {
    void execute(StartHandInputData inputData);
}
