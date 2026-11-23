package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The state of a poker game: the players, the deck, the community cards, the pot,
 * and whose turn it is. The human player is always at index 0.
 * This class only stores state; the rules of play live in the use case interactors.
 */
public class Game {

    private static final int MAX_LOG_SIZE = 10;

    private final List<Player> players;
    private final List<Card> communityCards;
    private final List<String> actionLog;
    private final List<Player> winners;
    private final int smallBlind;
    private final int bigBlind;
    private Deck deck;
    private int pot;
    private int dealerIndex;
    private int currentPlayerIndex;
    private int currentHighestBet;
    private int playersLeftToAct;
    private Stage stage;
    private HandCategory winningCategory;
    private int amountWon;

    /**
     * Creates a game that has not started its first hand yet.
     *
     * @param players the players at the table, with the human at index 0
     * @param smallBlind the small blind amount
     * @param bigBlind the big blind amount
     */
    public Game(List<Player> players, int smallBlind, int bigBlind) {
        this.players = new ArrayList<>(players);
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.communityCards = new ArrayList<>();
        this.actionLog = new ArrayList<>();
        this.winners = new ArrayList<>();
        this.deck = new Deck();
        this.pot = 0;
        this.dealerIndex = 0;
        this.currentPlayerIndex = 0;
        this.currentHighestBet = 0;
        this.playersLeftToAct = 0;
        this.stage = Stage.HAND_OVER;
        this.winningCategory = null;
        this.amountWon = 0;
    }

    /**
     * Returns a copy of the list of players. The Player objects themselves are shared.
     *
     * @return the players in seat order, human first
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Player getHumanPlayer() {
        return players.get(0);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Removes a player from the table, for example when they run out of chips.
     *
     * @param player the player to remove
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    /**
     * Returns a copy of the community cards on the table.
     *
     * @return the community cards, in the order they were dealt
     */
    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }

    public void addCommunityCard(Card card) {
        communityCards.add(card);
    }

    public void clearCommunityCards() {
        communityCards.clear();
    }

    public int getPot() {
        return pot;
    }

    public void addToPot(int amount) {
        pot = pot + amount;
    }

    public void clearPot() {
        pot = 0;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getCurrentHighestBet() {
        return currentHighestBet;
    }

    public void setCurrentHighestBet(int currentHighestBet) {
        this.currentHighestBet = currentHighestBet;
    }

    /**
     * Returns how many more players need to act before this betting round is over.
     *
     * @return the number of players still to act this round
     */
    public int getPlayersLeftToAct() {
        return playersLeftToAct;
    }

    public void setPlayersLeftToAct(int playersLeftToAct) {
        this.playersLeftToAct = playersLeftToAct;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Returns how many more chips the player must put in to match the highest bet.
     *
     * @param player the player to check
     * @return the amount to call, or 0 if the player has already matched the bet
     */
    public int getAmountToCall(Player player) {
        return Math.max(0, currentHighestBet - player.getCurrentBet());
    }

    /**
     * Returns the smallest total bet a player may raise to: the highest bet plus one big blind.
     *
     * @return the minimum raise-to amount
     */
    public int getMinimumRaiseTo() {
        return currentHighestBet + bigBlind;
    }

    /**
     * Returns a copy of the most recent actions, oldest first.
     *
     * @return up to the last 10 log messages
     */
    public List<String> getActionLog() {
        return new ArrayList<>(actionLog);
    }

    /**
     * Adds a message such as "Bot 2 raises to 40" to the log, dropping the oldest
     * message if the log is full.
     *
     * @param message the message to add
     */
    public void addToLog(String message) {
        actionLog.add(message);
        if (actionLog.size() > MAX_LOG_SIZE) {
            actionLog.remove(0);
        }
    }

    public void clearLog() {
        actionLog.clear();
    }

    /**
     * Records who won the hand that just finished.
     *
     * @param handWinners the players who won (more than one means a split pot)
     * @param category the winning hand category, or null if everyone else folded
     * @param amount the number of chips each winner received
     */
    public void setHandResult(List<Player> handWinners, HandCategory category, int amount) {
        winners.clear();
        winners.addAll(handWinners);
        winningCategory = category;
        amountWon = amount;
    }

    public void clearHandResult() {
        winners.clear();
        winningCategory = null;
        amountWon = 0;
    }

    public List<Player> getWinners() {
        return new ArrayList<>(winners);
    }

    public HandCategory getWinningCategory() {
        return winningCategory;
    }

    public int getAmountWon() {
        return amountWon;
    }
}
