package use_case.start_hand;

/**
 * Receives the result of starting a hand. Implemented by the presenter.
 */
public interface StartHandOutputBoundary {
    void prepareSuccessView(StartHandOutputData outputData);

    void prepareFailView(String errorMessage);
}
