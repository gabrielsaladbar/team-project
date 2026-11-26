package main.interface_adapters.game;

import main.interface_adapters.ViewManagerModel;
import main.interface_adapters.settings.SettingsViewModel;
import use_case.GameStateOutputData;
import use_case.player_action.PlayerActionOutputBoundary;
import use_case.player_action.PlayerActionOutputData;
import use_case.start_hand.StartHandOutputBoundary;
import use_case.start_hand.StartHandOutputData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes the game state from the start hand and player action use cases and puts it
 * into the GameViewModel so the game screen can draw it. It only formats data:
 * all the poker rules live in the use cases.
 */
public class GamePresenter implements StartHandOutputBoundary, PlayerActionOutputBoundary {

    private static final String NO_CARD = "/cards/null_card.png";
    private static final int[] DENOMINATIONS = {1, 5, 25, 50, 100};
    // How many of each of the four smaller chips to keep, so small bets can be made.
    private static final int[] SMALL_CHIP_COUNTS = {10, 5, 5, 4};
    private static final int LOG_LINES_SHOWN = 3;

    private final GameViewModel gameViewModel;
    private final SettingsViewModel settingsViewModel;
    private final ViewManagerModel viewManagerModel;

    // Seat on the screen for each player name: 0 is the human, 1 and up are the opponents.
    private final Map<String, Integer> seatByName = new HashMap<>();

    public GamePresenter(GameViewModel gameViewModel, SettingsViewModel settingsViewModel,
                         ViewManagerModel viewManagerModel) {
        this.gameViewModel = gameViewModel;
        this.settingsViewModel = settingsViewModel;
        this.viewManagerModel = viewManagerModel;
    }

    /**
     * A new hand was dealt: draw the new cards, then let the human act.
     */
    @Override
    public void prepareSuccessView(StartHandOutputData outputData) {
        showTable(outputData);
        gameViewModel.beginDealing();
        showTurnAndMessage(outputData);
    }

    /**
     * The human's move (and any bot moves after it) were applied.
     */
    @Override
    public void prepareSuccessView(PlayerActionOutputData outputData) {
        showTable(outputData);
        showTurnAndMessage(outputData);
    }

    /**
     * Shows why the move or new hand was not allowed, and lets the human try again.
     */
    @Override
    public void prepareFailView(String errorMessage) {
        String state = gameViewModel.getTurnState();
        gameViewModel.setTurnState("none");
        gameViewModel.setTurnState(state);
        gameViewModel.setMessage(errorMessage);
    }

    /**
     * Converts card text from the use cases, like "As" or "Td", into the image
     * path used by the card labels, like "/cards/card_spades_14.png".
     *
     * @param card a rank symbol followed by a suit symbol
     * @return the path of that card's image
     */
    public static String cardImagePath(String card) {
        char rank = card.charAt(0);
        int value;
        if (rank == 'T') {
            value = 10;
        } else if (rank == 'J') {
            value = 11;
        } else if (rank == 'Q') {
            value = 12;
        } else if (rank == 'K') {
            value = 13;
        } else if (rank == 'A') {
            value = 14;
        } else {
            value = rank - '0';
        }

        char suit = card.charAt(1);
        String suitName;
        if (suit == 's') {
            suitName = "spades";
        } else if (suit == 'h') {
            suitName = "hearts";
        } else if (suit == 'd') {
            suitName = "diamonds";
        } else {
            suitName = "clubs";
        }

        String number = Integer.toString(value);
        if (value < 10) {
            number = "0" + number;
        }
        return "/cards/card_" + suitName + "_" + number + ".png";
    }

    private void showTable(GameStateOutputData data) {
        List<String> names = data.getPlayerNames();
        for (int i = 0; i < names.size(); i++) {
            if (!seatByName.containsKey(names.get(i))) {
                seatByName.put(names.get(i), i);
            }
        }

        gameViewModel.setDealer(seatOf(data, data.getDealerName()));
        showHumanPlayer(data);
        showOpponents(data);
        showCommunityCards(data.getCommunityCards());
    }

    private void showHumanPlayer(GameStateOutputData data) {
        String[] cards = data.getPlayerCards().get(0).split(" ");
        String status = "revealed";
        if (data.getPlayerFolded().get(0)) {
            status = "folded";
        }
        gameViewModel.setPlayerCards(cardImagePath(cards[0]), cardImagePath(cards[1]), status);

        int chips = data.getPlayerChips().get(0);
        gameViewModel.setPlayerChips(chips, splitIntoChips(chips));
        gameViewModel.setPlayerCurrentBet(data.getPlayerBets().get(0));
    }

    private void showOpponents(GameStateOutputData data) {
        // Opponents who ran out of chips are no longer in the game; show them as empty seats.
        boolean[] stillPlaying = new boolean[gameViewModel.getPlayers()];
        List<String> names = data.getPlayerNames();

        for (int i = 1; i < names.size(); i++) {
            int seat = seatOf(data, names.get(i));
            stillPlaying[seat] = true;
            int opponent = seat - 1;

            String cardText = data.getPlayerCards().get(i);
            if (data.getPlayerFolded().get(i)) {
                gameViewModel.setOpponentCards(opponent, NO_CARD, NO_CARD, "folded");
            } else if (cardText.equals(GameStateOutputData.HIDDEN_CARDS)) {
                gameViewModel.setOpponentCards(opponent, NO_CARD, NO_CARD, "hidden");
            } else {
                String[] cards = cardText.split(" ");
                gameViewModel.setOpponentCards(opponent, cardImagePath(cards[0]),
                        cardImagePath(cards[1]), "revealed");
            }
            gameViewModel.setOpponentChips(opponent, data.getPlayerChips().get(i));
        }

        for (int seat = 1; seat < stillPlaying.length; seat++) {
            if (!stillPlaying[seat]) {
                gameViewModel.setOpponentCards(seat - 1, NO_CARD, NO_CARD, "");
                gameViewModel.setOpponentChips(seat - 1, 0);
            }
        }
    }

    /**
     * Before the flop all five cards are face down; then the flop, turn and river
     * are turned over as they are dealt.
     */
    private void showCommunityCards(List<String> cards) {
        if (cards.size() < 3) {
            List<String> faceDown = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                faceDown.add(NO_CARD);
            }
            gameViewModel.setCommunityCardsHidden(faceDown);
            return;
        }
        gameViewModel.setFlop(cardImagePath(cards.get(0)), cardImagePath(cards.get(1)),
                cardImagePath(cards.get(2)));
        if (cards.size() >= 4) {
            gameViewModel.setTurn(cardImagePath(cards.get(3)));
        }
        if (cards.size() >= 5) {
            gameViewModel.setRiver(cardImagePath(cards.get(4)));
        }
    }

    private void showTurnAndMessage(GameStateOutputData data) {
        // Go through "none" first so the buttons and chip selector always refresh.
        gameViewModel.setTurnState("none");

        if (data.isHandOver()) {
            gameViewModel.setMessage(handResultText(data));
            gameViewModel.setTurnState("handOver");
            return;
        }

        gameViewModel.setMessage(recentActionsText(data));
        if (data.isHumanTurn()) {
            // The chip selector counts chips being added, not the total bet.
            int humanBet = data.getPlayerBets().get(0);
            gameViewModel.setCurrentBetToMatch(data.getMinimumRaiseTo() - humanBet);
            if (data.getAmountToCall() > 0) {
                gameViewModel.setTurnState("facingBet");
            } else {
                gameViewModel.setTurnState("unopened");
            }
        }
    }

    private String handResultText(GameStateOutputData data) {
        List<String> winners = data.getWinnerNames();
        String names = "";
        for (int i = 0; i < winners.size(); i++) {
            if (i > 0) {
                names = names + " and ";
            }
            names = names + displayName(data, winners.get(i));
        }

        String text;
        if (winners.size() == 1) {
            text = names + " wins $" + data.getAmountWon();
        } else {
            text = names + " split the pot, $" + data.getAmountWon() + " each";
        }
        if (!data.getWinningHand().isEmpty()) {
            text = text + " with " + data.getWinningHand().toLowerCase().replace('_', ' ');
        }
        return text;
    }

    private String recentActionsText(GameStateOutputData data) {
        String text = "Pot: $" + data.getPot();
        List<String> log = data.getActionLog();
        int start = Math.max(0, log.size() - LOG_LINES_SHOWN);
        for (int i = start; i < log.size(); i++) {
            text = text + "  |  " + useDisplayNames(data, log.get(i));
        }
        return text;
    }

    /**
     * The use cases call the bots "Bot 1", "Bot 2" and so on, but the screen shows
     * the names chosen in the lobby.
     */
    private String useDisplayNames(GameStateOutputData data, String line) {
        List<String> names = data.getPlayerNames();
        String result = line;
        for (int i = 1; i < names.size(); i++) {
            result = result.replace(names.get(i), displayName(data, names.get(i)));
        }
        return result;
    }

    private String displayName(GameStateOutputData data, String name) {
        int seat = seatOf(data, name);
        if (seat == 0) {
            return name;
        }
        Map<String, String> info = gameViewModel.players.get("player" + (seat - 1));
        if (info == null) {
            return name;
        }
        return info.get("Name");
    }

    private int seatOf(GameStateOutputData data, String name) {
        Integer seat = seatByName.get(name);
        if (seat == null) {
            return data.getPlayerNames().indexOf(name);
        }
        return seat;
    }

    /**
     * Splits a chip total into chip counts for the chip display and the raise selector.
     * A few of each smaller chip are kept so the player can still make small bets.
     */
    private Map<Integer, Integer> splitIntoChips(int total) {
        List<Integer> values = gameViewModel.getChipValues();
        int[] counts = new int[DENOMINATIONS.length];
        int remaining = total;

        for (int i = 0; i < SMALL_CHIP_COUNTS.length; i++) {
            int take = Math.min(SMALL_CHIP_COUNTS[i], remaining / values.get(i));
            counts[i] = take;
            remaining = remaining - take * values.get(i);
        }
        for (int i = DENOMINATIONS.length - 1; i >= 0; i--) {
            int take = remaining / values.get(i);
            counts[i] = counts[i] + take;
            remaining = remaining - take * values.get(i);
        }

        Map<Integer, Integer> chips = new HashMap<>();
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            chips.put(DENOMINATIONS[i], counts[i]);
        }
        return chips;
    }
}
