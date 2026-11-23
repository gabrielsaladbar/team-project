package use_case;

import entity.ActionType;
import entity.Card;
import entity.Game;
import entity.Player;
import entity.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the game screen needs to draw the table, as plain Strings, ints and
 * booleans. It copies values out of the Game, so no entity objects reach the
 * presenter. Player lists are in seat order, with the human first.
 */
public class GameStateOutputData {

    public static final String HIDDEN_CARDS = "?? ??";

    private final List<String> playerNames = new ArrayList<>();
    private final List<Integer> playerChips = new ArrayList<>();
    private final List<Integer> playerBets = new ArrayList<>();
    private final List<Boolean> playerFolded = new ArrayList<>();
    private final List<Boolean> playerAllIn = new ArrayList<>();
    private final List<String> playerCards = new ArrayList<>();
    private final List<String> communityCards = new ArrayList<>();
    private final List<String> legalActions = new ArrayList<>();
    private final List<String> actionLog;
    private final List<String> winnerNames = new ArrayList<>();
    private final String dealerName;
    private final int pot;
    private final String stage;
    private final String currentPlayerName;
    private final boolean humanTurn;
    private final int amountToCall;
    private final int minimumRaiseTo;
    private final boolean handOver;
    private final String winningHand;
    private final int amountWon;
    private final String humanResult;

    /**
     * Copies the current state of the game.
     *
     * @param game the game to describe
     * @param humanLegalActions the moves the human may make now (empty if it is not their turn)
     */
    public GameStateOutputData(Game game, List<ActionType> humanLegalActions) {
        List<Player> players = game.getPlayers();
        Player human = game.getHumanPlayer();
        boolean showdownReached = game.getStage() == Stage.SHOWDOWN
                || (game.getStage() == Stage.HAND_OVER && game.getWinningCategory() != null);

        for (Player player : players) {
            playerNames.add(player.getName());
            playerChips.add(player.getChips());
            playerBets.add(player.getCurrentBet());
            playerFolded.add(player.isFolded());
            playerAllIn.add(player.isAllIn());
            if (player == human || (showdownReached && !player.isFolded())) {
                playerCards.add(cardsToString(player.getHoleCards()));
            } else {
                playerCards.add(HIDDEN_CARDS);
            }
        }
        for (Card card : game.getCommunityCards()) {
            communityCards.add(card.toString());
        }
        for (ActionType action : humanLegalActions) {
            legalActions.add(action.toString());
        }

        actionLog = game.getActionLog();
        dealerName = players.get(game.getDealerIndex()).getName();
        pot = game.getPot();
        stage = game.getStage().toString();
        humanTurn = !humanLegalActions.isEmpty();
        amountToCall = Math.min(game.getAmountToCall(human), human.getChips());
        minimumRaiseTo = game.getMinimumRaiseTo();
        handOver = game.getStage() == Stage.HAND_OVER;

        if (handOver || game.getStage() == Stage.SHOWDOWN) {
            currentPlayerName = "";
        } else {
            currentPlayerName = game.getCurrentPlayer().getName();
        }

        List<Player> winners = game.getWinners();
        for (Player winner : winners) {
            winnerNames.add(winner.getName());
        }
        if (game.getWinningCategory() == null) {
            winningHand = "";
        } else {
            winningHand = game.getWinningCategory().toString();
        }
        amountWon = game.getAmountWon();

        if (!handOver) {
            humanResult = "";
        } else if (!winners.contains(human)) {
            humanResult = "LOSS";
        } else if (winners.size() > 1) {
            humanResult = "TIE";
        } else {
            humanResult = "WIN";
        }
    }

    private String cardsToString(List<Card> cards) {
        String result = "";
        for (Card card : cards) {
            if (!result.isEmpty()) {
                result = result + " ";
            }
            result = result + card;
        }
        return result;
    }

    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }

    public List<Integer> getPlayerChips() {
        return new ArrayList<>(playerChips);
    }

    public List<Integer> getPlayerBets() {
        return new ArrayList<>(playerBets);
    }

    public List<Boolean> getPlayerFolded() {
        return new ArrayList<>(playerFolded);
    }

    public List<Boolean> getPlayerAllIn() {
        return new ArrayList<>(playerAllIn);
    }

    /**
     * Returns each player's hole cards, such as "As Kd". Bots' cards are "?? ??"
     * unless the hand reached a showdown and they did not fold.
     *
     * @return the cards for each player, in seat order
     */
    public List<String> getPlayerCards() {
        return new ArrayList<>(playerCards);
    }

    public List<String> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }

    public List<String> getLegalActions() {
        return new ArrayList<>(legalActions);
    }

    public List<String> getActionLog() {
        return new ArrayList<>(actionLog);
    }

    public String getDealerName() {
        return dealerName;
    }

    public int getPot() {
        return pot;
    }

    public String getStage() {
        return stage;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public boolean isHumanTurn() {
        return humanTurn;
    }

    public int getAmountToCall() {
        return amountToCall;
    }

    public int getMinimumRaiseTo() {
        return minimumRaiseTo;
    }

    public boolean isHandOver() {
        return handOver;
    }

    public List<String> getWinnerNames() {
        return new ArrayList<>(winnerNames);
    }

    /**
     * Returns the winning hand category, such as "FLUSH", or "" if the hand is not
     * over or was won because everyone else folded.
     *
     * @return the winning hand category name
     */
    public String getWinningHand() {
        return winningHand;
    }

    public int getAmountWon() {
        return amountWon;
    }

    /**
     * Returns "WIN", "LOSS" or "TIE" for the human once the hand is over, or "" before then.
     *
     * @return the human's result for this hand
     */
    public String getHumanResult() {
        return humanResult;
    }
}
