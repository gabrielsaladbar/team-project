package use_case.start_hand;

import data_access.InMemoryGameDataAccessObject;
import entity.BotPersonality;
import entity.Game;
import entity.Player;
import entity.SimpleBotStrategy;
import entity.Stage;
import org.junit.jupiter.api.Test;
import use_case.CallingBotStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartHandInteractorTest {

    private final InMemoryGameDataAccessObject dataAccess = new InMemoryGameDataAccessObject();
    private final FakeStartHandPresenter presenter = new FakeStartHandPresenter();

    private StartHandInteractor makeInteractor() {
        return new StartHandInteractor(dataAccess, presenter, new CallingBotStrategy(), new Random(1));
    }

    private int totalChips(Game game) {
        int total = game.getPot();
        for (Player player : game.getPlayers()) {
            total = total + player.getChips() + player.getCurrentBet();
        }
        return total;
    }

    @Test
    void newGameHasHumanAndNamedBots() {
        makeInteractor().execute(new StartHandInputData("Alice", 3, 1000, 10, 20));

        StartHandOutputData output = presenter.getOutputData();
        assertNull(presenter.getErrorMessage());
        assertEquals(Arrays.asList("Alice", "Bot 1", "Bot 2", "Bot 3"), output.getPlayerNames());

        Game game = dataAccess.getCurrentGame();
        assertFalse(game.getHumanPlayer().isBot());
        for (int i = 1; i < 4; i++) {
            assertTrue(game.getPlayers().get(i).isBot());
            assertNotNull(game.getPlayers().get(i).getPersonality());
        }
    }

    @Test
    void blindsArePostedAndBotsActBeforeHuman() {
        makeInteractor().execute(new StartHandInputData("Alice", 3, 1000, 10, 20));
        StartHandOutputData output = presenter.getOutputData();

        // Alice deals, Bot 1 posts the small blind, Bot 2 the big blind,
        // Bot 3 acts first and calls, then it is Alice's turn.
        assertEquals("Alice", output.getDealerName());
        assertEquals(Arrays.asList(0, 10, 20, 20), output.getPlayerBets());
        assertEquals(Arrays.asList(1000, 990, 980, 980), output.getPlayerChips());
        assertTrue(output.getActionLog().contains("Bot 1 posts small blind 10"));
        assertTrue(output.getActionLog().contains("Bot 2 posts big blind 20"));
        assertTrue(output.getActionLog().contains("Bot 3 calls 20"));

        assertEquals("PRE_FLOP", output.getStage());
        assertTrue(output.isHumanTurn());
        assertEquals("Alice", output.getCurrentPlayerName());
        assertEquals(20, output.getAmountToCall());
        assertEquals(40, output.getMinimumRaiseTo());
        assertEquals(Arrays.asList("FOLD", "CALL", "RAISE"), output.getLegalActions());
    }

    @Test
    void headsUpDealerPostsSmallBlindAndActsFirst() {
        makeInteractor().execute(new StartHandInputData("Alice", 1, 1000, 10, 20));
        StartHandOutputData output = presenter.getOutputData();

        assertEquals(Arrays.asList(10, 20), output.getPlayerBets());
        assertTrue(output.isHumanTurn());
        assertEquals(10, output.getAmountToCall());
    }

    @Test
    void dealsTwoCardsEachForEveryBotCount() {
        for (int bots = 1; bots <= 5; bots++) {
            makeInteractor().execute(new StartHandInputData("Alice", bots, 1000, 10, 20));
            Game game = dataAccess.getCurrentGame();
            StartHandOutputData output = presenter.getOutputData();

            assertEquals(52 - 2 * (bots + 1), game.getDeck().cardsRemaining());
            for (Player player : game.getPlayers()) {
                assertEquals(2, player.getHoleCards().size());
            }

            List<String> cards = output.getPlayerCards();
            assertNotEquals("?? ??", cards.get(0));
            assertEquals(5, cards.get(0).length());
            for (int i = 1; i < cards.size(); i++) {
                assertEquals("?? ??", cards.get(i));
            }
            assertTrue(output.getCommunityCards().isEmpty());
        }
    }

    @Test
    void rejectsBadBotCount() {
        makeInteractor().execute(new StartHandInputData("Alice", 0, 1000, 10, 20));
        assertEquals("Number of bots must be between 1 and 5.", presenter.getErrorMessage());

        makeInteractor().execute(new StartHandInputData("Alice", 6, 1000, 10, 20));
        assertEquals("Number of bots must be between 1 and 5.", presenter.getErrorMessage());

        assertNull(presenter.getOutputData());
        assertNull(dataAccess.getCurrentGame());
    }

    @Test
    void rejectsBadNameAndBlinds() {
        makeInteractor().execute(new StartHandInputData("  ", 2, 1000, 10, 20));
        assertEquals("Please enter a name.", presenter.getErrorMessage());

        makeInteractor().execute(new StartHandInputData("Alice", 2, 1000, 0, 20));
        assertEquals("The small blind must be more than 0.", presenter.getErrorMessage());

        makeInteractor().execute(new StartHandInputData("Alice", 2, 1000, 20, 10));
        assertEquals("The big blind must be at least the small blind.", presenter.getErrorMessage());

        makeInteractor().execute(new StartHandInputData("Alice", 2, 10, 10, 20));
        assertEquals("Starting chips must be at least the big blind.", presenter.getErrorMessage());
    }

    @Test
    void nextHandWithoutGameFails() {
        makeInteractor().execute(StartHandInputData.nextHand());
        assertEquals("There is no game in progress.", presenter.getErrorMessage());
    }

    @Test
    void nextHandBeforeHandIsOverFails() {
        makeInteractor().execute(new StartHandInputData("Alice", 2, 1000, 10, 20));
        makeInteractor().execute(StartHandInputData.nextHand());
        assertEquals("The current hand is not finished yet.", presenter.getErrorMessage());
    }

    @Test
    void nextHandMovesDealerAndRemovesBustedPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 1000, false));
        players.add(new Player("Bot 1", 0, BotPersonality.BALANCED));
        players.add(new Player("Bot 2", 1000, BotPersonality.BALANCED));
        players.add(new Player("Bot 3", 1000, BotPersonality.BALANCED));
        Game game = new Game(players, 10, 20);
        game.setDealerIndex(0);
        dataAccess.save(game);

        makeInteractor().execute(StartHandInputData.nextHand());

        StartHandOutputData output = presenter.getOutputData();
        assertNull(presenter.getErrorMessage());
        assertEquals(Arrays.asList("Alice", "Bot 2", "Bot 3"), output.getPlayerNames());
        // Bot 1 was next to deal but is out, so the button moves on to Bot 2.
        assertEquals("Bot 2", output.getDealerName());
        assertTrue(output.getActionLog().contains("Bot 1 is out of chips"));
        assertEquals(Stage.PRE_FLOP, dataAccess.getCurrentGame().getStage());
    }

    @Test
    void nextHandFailsWhenHumanIsBroke() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 0, false));
        players.add(new Player("Bot 1", 2000, BotPersonality.BALANCED));
        dataAccess.save(new Game(players, 10, 20));

        makeInteractor().execute(StartHandInputData.nextHand());
        assertEquals("You are out of chips. Game over.", presenter.getErrorMessage());
    }

    @Test
    void nextHandFailsWhenAllBotsAreBroke() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 2000, false));
        players.add(new Player("Bot 1", 0, BotPersonality.BALANCED));
        dataAccess.save(new Game(players, 10, 20));

        makeInteractor().execute(StartHandInputData.nextHand());
        assertEquals("Every bot is out of chips. You won the game!", presenter.getErrorMessage());
    }

    @Test
    void realBotsAlwaysStopOnHumanTurnOrHandEnd() {
        for (int seed = 0; seed < 100; seed++) {
            StartHandInteractor interactor = new StartHandInteractor(dataAccess, presenter,
                    new SimpleBotStrategy(new Random(seed)), new Random(seed));
            interactor.execute(new StartHandInputData("Alice", 5, 500, 5, 10));

            Game game = dataAccess.getCurrentGame();
            StartHandOutputData output = presenter.getOutputData();
            assertTrue(output.isHumanTurn() || output.isHandOver());
            assertEquals(6 * 500, totalChips(game));
        }
    }
}
