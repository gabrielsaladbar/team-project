package use_case.player_action;

import entity.BotStrategy;
import entity.Game;
import entity.Player;
import entity.Stage;
import use_case.GameDataAccessInterface;
import use_case.HandManager;

/**
 * Applies the human's move, then lets the bots play until it is the human's turn
 * again or the hand is over.
 *
 * Simplifications to keep the game small: there are no side pots, so an all-in
 * player can win the whole pot; and an all-in player stays in the hand but is
 * skipped for any further betting.
 */
public class PlayerActionInteractor implements PlayerActionInputBoundary {

    private final GameDataAccessInterface gameDataAccess;
    private final PlayerActionOutputBoundary presenter;
    private final HandManager handManager;

    /**
     * Creates the interactor.
     *
     * @param gameDataAccess where the current game is stored
     * @param presenter receives the result
     * @param botStrategy decides what the bots do
     */
    public PlayerActionInteractor(GameDataAccessInterface gameDataAccess,
                                  PlayerActionOutputBoundary presenter,
                                  BotStrategy botStrategy) {
        this.gameDataAccess = gameDataAccess;
        this.presenter = presenter;
        this.handManager = new HandManager(botStrategy);
    }

    @Override
    public void execute(PlayerActionInputData inputData) {
        Game game = gameDataAccess.getCurrentGame();
        if (game == null) {
            presenter.prepareFailView("There is no game in progress.");
            return;
        }
        if (game.getStage() == Stage.HAND_OVER) {
            presenter.prepareFailView("The hand is over. Start the next hand.");
            return;
        }
        if (handManager.getHumanLegalActions(game).isEmpty()) {
            presenter.prepareFailView("It is not your turn.");
            return;
        }

        Player human = game.getHumanPlayer();
        String problem = handManager.getIllegalMoveMessage(game, human,
                inputData.getActionType(), inputData.getRaiseAmount());
        if (problem != null) {
            presenter.prepareFailView(problem);
            return;
        }

        handManager.takeTurn(game, inputData.getActionType(), inputData.getRaiseAmount());
        handManager.playUntilHumanTurn(game);
        gameDataAccess.save(game);
        presenter.prepareSuccessView(new PlayerActionOutputData(game, handManager.getHumanLegalActions(game)));
    }
}
