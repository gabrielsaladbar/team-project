package use_case.player_action;

import data_access.InMemoryGameDataAccessObject;
import entity.ActionType;
import entity.BotStrategy;
import entity.Game;
import entity.Player;
import entity.SimpleBotStrategy;
import entity.Stage;
import org.junit.jupiter.api.Test;
import use_case.CallingBotStrategy;
import use_case.FoldingBotStrategy;
import use_case.start_hand.FakeStartHandPresenter;
import use_case.start_hand.StartHandInputData;
import use_case.start_hand.StartHandInteractor;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerActionInteractorTest {

    private final InMemoryGameDataAccessObject dataAccess = new InMemoryGameDataAccessObject();
    private final FakePlayerActionPresenter presenter = new FakePlayerActionPresenter();

    /**
     * Starts a hand where Alice deals. With 2 bots, Bot 1 posts 10, Bot 2 posts 20,
     * and Alice acts first.
     */
    private PlayerActionInteractor startHand(int bots, BotStrategy strategy) {
        StartHandInteractor startHand = new StartHandInteractor(dataAccess,
                new FakeStartHandPresenter(), strategy, new Random(3));
        startHand.execute(new StartHandInputData("Alice", bots, 1000, 10, 20));
        return new PlayerActionInteractor(dataAccess, presenter, strategy);
    }

    private void act(PlayerActionInteractor interactor, ActionType type, int amount) {
        interactor.execute(new PlayerActionInputData(type, amount));
    }

    private int totalChips(Game game) {
        int total = game.getPot();
        for (Player player : game.getPlayers()) {
            total = total + player.getChips() + player.getCurrentBet();
        }
        return total;
    }

    @Test
    void checkingWithABetToCallIsRejected() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.CHECK, 0);

        assertEquals("You cannot check when there is a bet of 20 to call.", presenter.getErrorMessage());
        assertNull(presenter.getOutputData());
        assertEquals(0, dataAccess.getCurrentGame().getHumanPlayer().getCurrentBet());
    }

    @Test
    void raiseBelowMinimumIsRejected() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.RAISE, 30);
        assertEquals("The minimum raise is to 40.", presenter.getErrorMessage());
    }

    @Test
    void raisingMoreThanYouHaveIsRejected() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.RAISE, 5000);
        assertEquals("You do not have enough chips to raise to 5000.", presenter.getErrorMessage());
    }

    @Test
    void callingWithNothingToCallIsRejected() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.CALL, 0);
        assertEquals("FLOP", presenter.getOutputData().getStage());

        act(interactor, ActionType.CALL, 0);
        assertEquals("There is no bet to call. You can check instead.", presenter.getErrorMessage());
    }

    @Test
    void noGameIsRejected() {
        PlayerActionInteractor interactor = new PlayerActionInteractor(dataAccess, presenter,
                new CallingBotStrategy());
        act(interactor, ActionType.FOLD, 0);
        assertEquals("There is no game in progress.", presenter.getErrorMessage());
    }

    @Test
    void foldingLeavesRemainingPlayerWinningThePot() {
        // Heads up: Alice deals, posts the small blind and acts first.
        PlayerActionInteractor interactor = startHand(1, new CallingBotStrategy());
        act(interactor, ActionType.FOLD, 0);

        PlayerActionOutputData output = presenter.getOutputData();
        assertTrue(output.isHandOver());
        assertEquals("HAND_OVER", output.getStage());
        assertEquals(Arrays.asList("Bot 1"), output.getWinnerNames());
        assertEquals(30, output.getAmountWon());
        assertEquals("", output.getWinningHand());
        assertEquals("LOSS", output.getHumanResult());
        assertEquals(Arrays.asList(990, 1010), output.getPlayerChips());
        assertEquals(0, output.getPot());
        // nobody showed their cards
        assertEquals("?? ??", output.getPlayerCards().get(1));

        act(interactor, ActionType.CHECK, 0);
        assertEquals("The hand is over. Start the next hand.", presenter.getErrorMessage());
    }

    @Test
    void botsFoldingToARaiseGiveTheHumanThePot() {
        PlayerActionInteractor interactor = startHand(2, new FoldingBotStrategy());
        act(interactor, ActionType.RAISE, 60);

        PlayerActionOutputData output = presenter.getOutputData();
        assertTrue(output.isHandOver());
        assertEquals(Arrays.asList("Alice"), output.getWinnerNames());
        assertEquals(90, output.getAmountWon());
        assertEquals("WIN", output.getHumanResult());
        assertEquals(Arrays.asList(1030, 990, 980), output.getPlayerChips());
        assertTrue(output.getActionLog().contains("Alice raises to 60"));
        assertTrue(output.getActionLog().contains("Bot 1 folds"));
    }

    @Test
    void raiseIsCalledAndBetsMoveIntoThePot() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.RAISE, 60);

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals("FLOP", output.getStage());
        assertEquals(180, output.getPot());
        assertEquals(Arrays.asList(0, 0, 0), output.getPlayerBets());
        assertEquals(Arrays.asList(940, 940, 940), output.getPlayerChips());
        assertTrue(output.getActionLog().contains("Bot 1 calls 50"));
        assertTrue(output.getActionLog().contains("Bot 2 calls 40"));
    }

    @Test
    void stagesAdvanceInOrder() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());

        act(interactor, ActionType.CALL, 0);
        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals("FLOP", output.getStage());
        assertEquals(3, output.getCommunityCards().size());
        assertEquals(60, output.getPot());
        assertTrue(output.isHumanTurn());
        assertEquals(Arrays.asList("FOLD", "CHECK", "RAISE"), output.getLegalActions());

        act(interactor, ActionType.CHECK, 0);
        output = presenter.getOutputData();
        assertEquals("TURN", output.getStage());
        assertEquals(4, output.getCommunityCards().size());

        act(interactor, ActionType.CHECK, 0);
        output = presenter.getOutputData();
        assertEquals("RIVER", output.getStage());
        assertEquals(5, output.getCommunityCards().size());
        assertTrue(output.isHumanTurn());
    }

    @Test
    void allInPlayerIsSkippedAndCardsAreDealtOut() {
        PlayerActionInteractor interactor = startHand(2, new CallingBotStrategy());
        act(interactor, ActionType.RAISE, 1000);

        PlayerActionOutputData output = presenter.getOutputData();
        assertTrue(output.getPlayerAllIn().get(0));
        assertFalse(output.isHumanTurn());
        assertEquals(5, output.getCommunityCards().size());
        assertEquals(3000, totalChips(dataAccess.getCurrentGame()));
    }

    @Test
    void handsWithRealBotsNeverBreakTheRules() {
        for (int seed = 0; seed < 100; seed++) {
            BotStrategy strategy = new SimpleBotStrategy(new Random(seed));
            StartHandInteractor startHand = new StartHandInteractor(dataAccess,
                    new FakeStartHandPresenter(), strategy, new Random(seed));
            startHand.execute(new StartHandInputData("Alice", 4, 300, 5, 10));
            FakePlayerActionPresenter seedPresenter = new FakePlayerActionPresenter();
            PlayerActionInteractor interactor = new PlayerActionInteractor(dataAccess, seedPresenter, strategy);
            Game game = dataAccess.getCurrentGame();

            // Alice checks or calls until the interactor says it is no longer her turn.
            for (int moves = 0; moves < 50 && seedPresenter.getErrorMessage() == null; moves++) {
                ActionType move = ActionType.CALL;
                if (game.getAmountToCall(game.getHumanPlayer()) == 0) {
                    move = ActionType.CHECK;
                }
                act(interactor, move, 0);
            }
            assertEquals(5 * 300, totalChips(game));
            assertEquals(Stage.HAND_OVER, game.getStage());
        }
    }
}
