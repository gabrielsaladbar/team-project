package use_case.start_hand;

import entity.BotPersonality;
import entity.BotStrategy;
import entity.Deck;
import entity.Game;
import entity.Player;
import entity.Stage;
import use_case.GameDataAccessInterface;
import use_case.HandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Starts a new game or the next hand: resets the players, shuffles a new deck,
 * posts the blinds, deals two cards each, and lets the bots act until it is the
 * human's turn.
 */
public class StartHandInteractor implements StartHandInputBoundary {

    private static final int MIN_BOTS = 1;
    private static final int MAX_BOTS = 5;

    private final GameDataAccessInterface gameDataAccess;
    private final StartHandOutputBoundary presenter;
    private final HandManager handManager;
    private final Random random;

    /**
     * Creates the interactor.
     *
     * @param gameDataAccess where the current game is stored
     * @param presenter receives the result
     * @param botStrategy decides what the bots do
     * @param random used to shuffle the deck and pick bot personalities
     */
    public StartHandInteractor(GameDataAccessInterface gameDataAccess,
                               StartHandOutputBoundary presenter,
                               BotStrategy botStrategy, Random random) {
        this.gameDataAccess = gameDataAccess;
        this.presenter = presenter;
        this.handManager = new HandManager(botStrategy);
        this.random = random;
    }

    @Override
    public void execute(StartHandInputData inputData) {
        Game game;
        if (inputData.isNextHand()) {
            game = gameDataAccess.getCurrentGame();
            String problem = checkCanStartNextHand(game);
            if (problem != null) {
                presenter.prepareFailView(problem);
                return;
            }
            prepareTableForNextHand(game);
        } else {
            String problem = checkNewGameInput(inputData);
            if (problem != null) {
                presenter.prepareFailView(problem);
                return;
            }
            game = createGame(inputData);
        }

        dealNewHand(game);
        handManager.playUntilHumanTurn(game);
        gameDataAccess.save(game);
        presenter.prepareSuccessView(new StartHandOutputData(game, handManager.getHumanLegalActions(game)));
    }

    /**
     * Returns an error message if the new game settings are not allowed, or null if they are fine.
     */
    private String checkNewGameInput(StartHandInputData inputData) {
        if (inputData.getHumanName() == null || inputData.getHumanName().trim().isEmpty()) {
            return "Please enter a name.";
        }
        if (inputData.getNumberOfBots() < MIN_BOTS || inputData.getNumberOfBots() > MAX_BOTS) {
            return "Number of bots must be between " + MIN_BOTS + " and " + MAX_BOTS + ".";
        }
        if (inputData.getSmallBlind() <= 0) {
            return "The small blind must be more than 0.";
        }
        if (inputData.getBigBlind() < inputData.getSmallBlind()) {
            return "The big blind must be at least the small blind.";
        }
        if (inputData.getStartingChips() < inputData.getBigBlind()) {
            return "Starting chips must be at least the big blind.";
        }
        return null;
    }

    /**
     * Returns an error message if the next hand cannot be dealt, or null if it can.
     */
    private String checkCanStartNextHand(Game game) {
        if (game == null) {
            return "There is no game in progress.";
        }
        if (game.getStage() != Stage.HAND_OVER) {
            return "The current hand is not finished yet.";
        }
        if (game.getHumanPlayer().getChips() == 0) {
            return "You are out of chips. Game over.";
        }
        boolean botHasChips = false;
        for (Player player : game.getPlayers()) {
            if (player.isBot() && player.getChips() > 0) {
                botHasChips = true;
            }
        }
        if (!botHasChips) {
            return "Every bot is out of chips. You won the game!";
        }
        return null;
    }

    private Game createGame(StartHandInputData inputData) {
        List<Player> players = new ArrayList<>();
        int chips = inputData.getStartingChips();
        players.add(new Player(inputData.getHumanName().trim(), chips, false));

        BotPersonality[] personalities = BotPersonality.values();
        for (int i = 1; i <= inputData.getNumberOfBots(); i++) {
            BotPersonality personality = personalities[random.nextInt(personalities.length)];
            players.add(new Player("Bot " + i, chips, personality));
        }
        return new Game(players, inputData.getSmallBlind(), inputData.getBigBlind());
    }

    /**
     * Moves the dealer one seat to the left (skipping busted players) and removes
     * players who have no chips left.
     */
    private void prepareTableForNextHand(Game game) {
        game.clearLog();
        List<Player> players = game.getPlayers();
        int oldDealer = game.getDealerIndex();

        Player newDealer = null;
        for (int step = 1; step <= players.size() && newDealer == null; step++) {
            Player candidate = players.get((oldDealer + step) % players.size());
            if (candidate.getChips() > 0) {
                newDealer = candidate;
            }
        }

        for (Player player : players) {
            if (player.getChips() == 0) {
                game.removePlayer(player);
                game.addToLog(player.getName() + " is out of chips");
            }
        }
        game.setDealerIndex(game.getPlayers().indexOf(newDealer));
    }

    private void dealNewHand(Game game) {
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            player.resetForNewHand();
        }
        game.clearCommunityCards();
        game.clearPot();
        game.clearHandResult();

        Deck deck = new Deck(random);
        deck.shuffle();
        game.setDeck(deck);
        for (Player player : players) {
            player.receiveCards(deck.deal(), deck.deal());
        }
        game.setStage(Stage.PRE_FLOP);

        // With only two players the dealer posts the small blind.
        int dealer = game.getDealerIndex();
        int smallBlindIndex = (dealer + 1) % players.size();
        if (players.size() == 2) {
            smallBlindIndex = dealer;
        }
        int bigBlindIndex = (smallBlindIndex + 1) % players.size();

        Player smallBlindPlayer = players.get(smallBlindIndex);
        Player bigBlindPlayer = players.get(bigBlindIndex);
        postBlind(game, smallBlindPlayer, game.getSmallBlind(), "small");
        postBlind(game, bigBlindPlayer, game.getBigBlind(), "big");
        game.setCurrentHighestBet(Math.max(smallBlindPlayer.getCurrentBet(), bigBlindPlayer.getCurrentBet()));

        handManager.startBettingRound(game, bigBlindIndex);
    }

    private void postBlind(Game game, Player player, int blind, String blindName) {
        int amount = Math.min(blind, player.getChips());
        player.bet(amount);
        game.addToLog(player.getName() + " posts " + blindName + " blind " + amount);
    }
}
