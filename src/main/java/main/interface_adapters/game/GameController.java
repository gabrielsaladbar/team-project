package main.interface_adapters.game;

import entity.ActionType;
import use_case.player_action.PlayerActionInputBoundary;
import use_case.player_action.PlayerActionInputData;
import use_case.start_hand.StartHandInputBoundary;
import use_case.start_hand.StartHandInputData;

import java.util.Map;

/**
 * Turns button clicks on the game screen into calls to the start hand and
 * player action use cases.
 */
public class GameController {

    private final StartHandInputBoundary startHandInteractor;
    private final PlayerActionInputBoundary playerActionInteractor;
    private final GameViewModel gameViewModel;

    public GameController(StartHandInputBoundary startHandInteractor,
                          PlayerActionInputBoundary playerActionInteractor,
                          GameViewModel gameViewModel) {
        this.startHandInteractor = startHandInteractor;
        this.playerActionInteractor = playerActionInteractor;
        this.gameViewModel = gameViewModel;
    }

    /**
     * Starts a new game using the settings chosen in the lobby.
     * The small blind is the lobby's minimum bet and the big blind is twice that.
     */
    public void gameStarted() {
        String name = gameViewModel.getName();
        int numberOfBots = gameViewModel.getPlayers() - 1;
        int startingChips = gameViewModel.getChipsHeld();
        int smallBlind = gameViewModel.getChipValues().get(0);
        int bigBlind = smallBlind * 2;
        startHandInteractor.execute(new StartHandInputData(name, numberOfBots, startingChips,
                smallBlind, bigBlind));
    }

    /**
     * Deals the next hand of the current game.
     */
    public void nextHand() {
        startHandInteractor.execute(StartHandInputData.nextHand());
    }

    public void playerCheck() {
        playerActionInteractor.execute(new PlayerActionInputData(ActionType.CHECK, 0));
    }

    public void playerCall() {
        playerActionInteractor.execute(new PlayerActionInputData(ActionType.CALL, 0));
    }

    /**
     * Raises by putting the given number of chips in. The use case wants the total
     * bet to raise to, so the chips already bet this round are added on.
     *
     * @param amount the chips being added to the bet
     */
    public void playerRaise(int amount) {
        int raiseTo = gameViewModel.getPlayerCurrentBet() + amount;
        playerActionInteractor.execute(new PlayerActionInputData(ActionType.RAISE, raiseTo));
    }

    public void playerReraise(int amount) {
        playerRaise(amount);
    }

    public void playerFold() {
        playerActionInteractor.execute(new PlayerActionInputData(ActionType.FOLD, 0));
    }

    /**
     * Raise from the chip selector. The chip counts are only for the display,
     * so only the total amount is used.
     */
    public void playerRaise(Map<Integer, Integer> betChips, int totalAmount) {
        playerRaise(totalAmount);
    }

    public void playerReraise(Map<Integer, Integer> betChips, int totalAmount) {
        playerRaise(totalAmount);
    }

}
