package use_case.player_action;

/**
 * Records what the interactor sends, so tests can check it.
 */
public class FakePlayerActionPresenter implements PlayerActionOutputBoundary {

    private PlayerActionOutputData outputData = null;
    private String errorMessage = null;

    @Override
    public void prepareSuccessView(PlayerActionOutputData outputData) {
        this.outputData = outputData;
        this.errorMessage = null;
    }

    @Override
    public void prepareFailView(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public PlayerActionOutputData getOutputData() {
        return outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
