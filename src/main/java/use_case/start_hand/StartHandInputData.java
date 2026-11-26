package use_case.start_hand;

/**
 * The input for starting a hand: either the settings for a brand new game, or a
 * request to deal the next hand of the game already in progress.
 */
public class StartHandInputData {

    private final boolean nextHand;
    private final String humanName;
    private final int numberOfBots;
    private final int startingChips;
    private final int smallBlind;
    private final int bigBlind;

    /**
     * Creates input for a new game.
     *
     * @param humanName the human player's name
     * @param numberOfBots how many bots to play against (1 to 5)
     * @param startingChips the chips every player starts with
     * @param smallBlind the small blind amount
     * @param bigBlind the big blind amount
     */
    public StartHandInputData(String humanName, int numberOfBots, int startingChips,
                              int smallBlind, int bigBlind) {
        this.nextHand = false;
        this.humanName = humanName;
        this.numberOfBots = numberOfBots;
        this.startingChips = startingChips;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    private StartHandInputData() {
        this.nextHand = true;
        this.humanName = "";
        this.numberOfBots = 0;
        this.startingChips = 0;
        this.smallBlind = 0;
        this.bigBlind = 0;
    }

    /**
     * Creates input asking for the next hand of the current game.
     *
     * @return input data for the next hand
     */
    public static StartHandInputData nextHand() {
        return new StartHandInputData();
    }

    public boolean isNextHand() {
        return nextHand;
    }

    public String getHumanName() {
        return humanName;
    }

    public int getNumberOfBots() {
        return numberOfBots;
    }

    public int getStartingChips() {
        return startingChips;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }
}
