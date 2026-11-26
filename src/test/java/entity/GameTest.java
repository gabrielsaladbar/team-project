package entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game makeGame() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 1000, false));
        players.add(new Player("Bot 1", 1000, BotPersonality.BALANCED));
        players.add(new Player("Bot 2", 1000, BotPersonality.CAUTIOUS));
        return new Game(players, 10, 20);
    }

    @Test
    void humanIsAtIndexZero() {
        Game game = makeGame();
        assertEquals("Alice", game.getHumanPlayer().getName());
        assertEquals(3, game.getPlayers().size());
        assertEquals(10, game.getSmallBlind());
        assertEquals(20, game.getBigBlind());
    }

    @Test
    void newGameIsWaitingForFirstHand() {
        Game game = makeGame();
        assertEquals(Stage.HAND_OVER, game.getStage());
        assertEquals(0, game.getPot());
        assertTrue(game.getCommunityCards().isEmpty());
        assertNull(game.getWinningCategory());
    }

    @Test
    void amountToCallAndMinimumRaise() {
        Game game = makeGame();
        Player bot = game.getPlayers().get(1);
        game.setCurrentHighestBet(40);
        bot.bet(10);
        assertEquals(30, game.getAmountToCall(bot));
        assertEquals(60, game.getMinimumRaiseTo());

        bot.bet(30);
        assertEquals(0, game.getAmountToCall(bot));
    }

    @Test
    void potAddsUpAndClears() {
        Game game = makeGame();
        game.addToPot(30);
        game.addToPot(20);
        assertEquals(50, game.getPot());
        game.clearPot();
        assertEquals(0, game.getPot());
    }

    @Test
    void logKeepsOnlyRecentMessages() {
        Game game = makeGame();
        for (int i = 1; i <= 15; i++) {
            game.addToLog("message " + i);
        }
        List<String> log = game.getActionLog();
        assertEquals(10, log.size());
        assertEquals("message 6", log.get(0));
        assertEquals("message 15", log.get(9));
    }

    @Test
    void removePlayerTakesThemOffTheTable() {
        Game game = makeGame();
        Player bot = game.getPlayers().get(1);
        game.removePlayer(bot);
        assertEquals(2, game.getPlayers().size());
        assertEquals("Bot 2", game.getPlayers().get(1).getName());
    }

    @Test
    void handResultIsStoredAndCleared() {
        Game game = makeGame();
        List<Player> winners = new ArrayList<>();
        winners.add(game.getHumanPlayer());
        game.setHandResult(winners, HandCategory.FLUSH, 300);
        assertEquals(1, game.getWinners().size());
        assertEquals(HandCategory.FLUSH, game.getWinningCategory());
        assertEquals(300, game.getAmountWon());

        game.clearHandResult();
        assertTrue(game.getWinners().isEmpty());
        assertNull(game.getWinningCategory());
        assertEquals(0, game.getAmountWon());
    }
}
