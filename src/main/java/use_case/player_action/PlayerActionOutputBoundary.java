package use_case.player_action;

/**
 * Receives the result of the human's move. Implemented by the presenter.
 */
public interface PlayerActionOutputBoundary {
    void prepareSuccessView(PlayerActionOutputData outputData);

    void prepareFailView(String errorMessage);
}
