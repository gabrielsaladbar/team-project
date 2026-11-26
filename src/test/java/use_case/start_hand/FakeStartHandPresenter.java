package use_case.start_hand;

/**
 * Records what the interactor sends, so tests can check it.
 */
public class FakeStartHandPresenter implements StartHandOutputBoundary {

    private StartHandOutputData outputData = null;
    private String errorMessage = null;

    @Override
    public void prepareSuccessView(StartHandOutputData outputData) {
        this.outputData = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public StartHandOutputData getOutputData() {
        return outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
