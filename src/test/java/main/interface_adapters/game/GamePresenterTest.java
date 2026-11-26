package main.interface_adapters.game;

import data_access.InMemoryGameDataAccessObject;
import entity.BotStrategy;
import entity.Card;
import entity.Game;
import org.junit.jupiter.api.Test;
import use_case.CallingBotStrategy;
import use_case.FoldingBotStrategy;
import use_case.player_action.PlayerActionInteractor;
import use_case.start_hand.StartHandInteractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamePresenterTest {

    private final GameViewModel viewModel = new GameViewModel();
    private final GamePresenter presenter = new GamePresenter(viewModel, null, null);
    private final InMemoryGameDataAccessObject dataAccess = new InMemoryGameDataAccessObject();

    /**
     * Sets up the view model like the lobby does (Alice and two opponents, minimum
     * bet 10) and starts a game through the controller. Alice acts first.
     */
    private GameController startGame(BotStrategy strategy) {
        viewModel.saveOpponentData(
                Arrays.asList("Jim", "Janiel", "Janthony", "Jmarcus", "Jabriel", "Jomas"),
                Arrays.asList("1000", "1000", "1000", "1000", "1000", "1000"));
        viewModel.setName("Alice");
        viewModel.setChipValues(10);
        viewModel.chips.put(100, 1);
        viewModel.setPlayers(3);
        viewModel.startGame();

        StartHandInteractor startHand = new StartHandInteractor(dataAccess, presenter, strategy,
                new Random(3));
        PlayerActionInteractor playerAction = new PlayerActionInteractor(dataAccess, presenter, strategy);
        GameController controller = new GameController(startHand, playerAction, viewModel);
        controller.gameStarted();
        return controller;
    }

    private String path(Card card) {
        return GamePresenter.cardImagePath(card.toString());
    }

    @Test
    void cardTextBecomesImagePath() {
        assertEquals("/cards/card_spades_14.png", GamePresenter.cardImagePath("As"));
        assertEquals("/cards/card_diamonds_10.png", GamePresenter.cardImagePath("Td"));
        assertEquals("/cards/card_hearts_13.png", GamePresenter.cardImagePath("Kh"));
        assertEquals("/cards/card_clubs_02.png", GamePresenter.cardImagePath("2c"));
        assertEquals("/cards/card_spades_09.png", GamePresenter.cardImagePath("9s"));
        assertEquals("/cards/card_hearts_11.png", GamePresenter.cardImagePath("Jh"));
    }

    @Test
    void newHandShowsHumanCardsAndHidesTheBoard() {
        startGame(new CallingBotStrategy());
        Game game = dataAccess.getCurrentGame();
        List<Card> aliceCards = game.getHumanPlayer().getHoleCards();

        assertEquals(path(aliceCards.get(0)), viewModel.getPlayerCard1());
        assertEquals(path(aliceCards.get(1)), viewModel.getPlayerCard2());
        assertEquals("revealed", viewModel.getPlayerCardStatus());
        assertEquals(Arrays.asList("hidden", "hidden", "hidden", "hidden", "hidden"),
                viewModel.getCommunityCardStatuses());
        assertEquals("hidden", viewModel.players.get("player0").get("CardStatus"));
        assertEquals("facingBet", viewModel.getTurnState());
        // the big blind is 20, so the smallest raise adds 40 chips
        assertEquals(40, viewModel.getCurrentBetToMatch());
        assertEquals("990", viewModel.players.get("player0").get("Chips"));
        assertEquals("980", viewModel.players.get("player1").get("Chips"));
    }

    @Test
    void flopTurnAndRiverAreTurnedOverByStage() {
        GameController controller = startGame(new CallingBotStrategy());
        Game game = dataAccess.getCurrentGame();

        controller.playerCall();
        assertEquals(Arrays.asList("revealed", "revealed", "revealed", "hidden", "hidden"),
                viewModel.getCommunityCardStatuses());
        assertEquals(path(game.getCommunityCards().get(0)), viewModel.getCommunityCards().get(0));
        assertEquals(path(game.getCommunityCards().get(2)), viewModel.getCommunityCards().get(2));
        assertEquals("unopened", viewModel.getTurnState());

        controller.playerCheck();
        assertEquals(Arrays.asList("revealed", "revealed", "revealed", "revealed", "hidden"),
                viewModel.getCommunityCardStatuses());
        assertEquals(path(game.getCommunityCards().get(3)), viewModel.getCommunityCards().get(3));

        controller.playerCheck();
        assertEquals(Arrays.asList("revealed", "revealed", "revealed", "revealed", "revealed"),
                viewModel.getCommunityCardStatuses());
        assertEquals(path(game.getCommunityCards().get(4)), viewModel.getCommunityCards().get(4));
    }

    @Test
    void raiseFromChipSelectorIsTurnedIntoRaiseTo() {
        GameController controller = startGame(new CallingBotStrategy());
        // Alice has nothing in yet, so adding 60 chips is a raise to 60.
        controller.playerRaise(new HashMap<Integer, Integer>(), 60);

        assertEquals(180, dataAccess.getCurrentGame().getPot());
        assertEquals(940, viewModel.getChipsHeld());
        assertEquals("940", viewModel.players.get("player1").get("Chips"));
    }

    @Test
    void handOverShowsWinnerAndNextHandButton() {
        GameController controller = startGame(new FoldingBotStrategy());
        controller.playerRaise(60);

        assertEquals("Alice wins $90", viewModel.getMessage());
        assertEquals("handOver", viewModel.getTurnState());
        assertEquals(1030, viewModel.getChipsHeld());
        assertEquals("folded", viewModel.players.get("player0").get("CardStatus"));

        controller.nextHand();
        assertEquals("hidden", viewModel.getCommunityCardStatuses().get(0));
        assertEquals("revealed", viewModel.getPlayerCardStatus());
    }

    @Test
    void illegalMoveShowsMessageAndKeepsTurn() {
        GameController controller = startGame(new CallingBotStrategy());
        controller.playerCheck();

        assertEquals("You cannot check when there is a bet of 20 to call.", viewModel.getMessage());
        assertEquals("facingBet", viewModel.getTurnState());
    }
}
