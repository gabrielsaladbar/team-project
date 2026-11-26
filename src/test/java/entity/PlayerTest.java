package entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PlayerTest {

    @Test
    void newPlayerHasNoCardsAndNoBet() {
        Player player = new Player("Alice", 1000, false);
        assertEquals("Alice", player.getName());
        assertEquals(1000, player.getChips());
        assertEquals(0, player.getCurrentBet());
        assertFalse(player.isFolded());
        assertFalse(player.isBot());
        assertTrue(player.getHoleCards().isEmpty());
    }

    @Test
    void betMovesChipsIntoCurrentBet() {
        Player player = new Player("Bot 1", 500, true);
        player.bet(100);
        player.bet(50);
        assertEquals(350, player.getChips());
        assertEquals(150, player.getCurrentBet());
        assertTrue(player.isBot());
    }

    @Test
    void canBetAllChips() {
        Player player = new Player("Alice", 200, false);
        player.bet(200);
        assertEquals(0, player.getChips());
        assertEquals(200, player.getCurrentBet());
    }

    @Test
    void overBettingThrowsAndChangesNothing() {
        Player player = new Player("Alice", 100, false);
        try {
            player.bet(101);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(100, player.getChips());
        assertEquals(0, player.getCurrentBet());
    }

    @Test
    void negativeBetThrows() {
        Player player = new Player("Alice", 100, false);
        try {
            player.bet(-5);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    void receiveCardsStoresTwoHoleCards() {
        Player player = new Player("Alice", 100, false);
        Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
        Card kingHearts = new Card(Rank.KING, Suit.HEARTS);
        player.receiveCards(aceSpades, kingHearts);

        List<Card> cards = player.getHoleCards();
        assertEquals(2, cards.size());
        assertEquals(aceSpades, cards.get(0));
        assertEquals(kingHearts, cards.get(1));
    }

    @Test
    void foldSetsFoldedFlag() {
        Player player = new Player("Alice", 100, false);
        player.fold();
        assertTrue(player.isFolded());
    }

    @Test
    void resetForNewHandClearsHandStateButKeepsChips() {
        Player player = new Player("Alice", 100, false);
        player.receiveCards(new Card(Rank.TWO, Suit.CLUBS), new Card(Rank.THREE, Suit.CLUBS));
        player.bet(40);
        player.fold();

        player.resetForNewHand();

        assertEquals(60, player.getChips());
        assertEquals(0, player.getCurrentBet());
        assertFalse(player.isFolded());
        assertTrue(player.getHoleCards().isEmpty());
    }

    @Test
    void botKeepsItsPersonalityAndHumanHasNone() {
        Player bot = new Player("Bot 1", 100, BotPersonality.AGGRESSIVE);
        assertTrue(bot.isBot());
        assertEquals(BotPersonality.AGGRESSIVE, bot.getPersonality());
        assertNull(new Player("Alice", 100, false).getPersonality());
    }

    @Test
    void allInWhenNoChipsLeftAndNotFolded() {
        Player player = new Player("Alice", 100, false);
        assertFalse(player.isAllIn());
        player.bet(100);
        assertTrue(player.isAllIn());
        player.fold();
        assertFalse(player.isAllIn());
    }

    @Test
    void resetBetForNewRoundKeepsCardsAndChips() {
        Player player = new Player("Alice", 100, false);
        player.receiveCards(new Card(Rank.TWO, Suit.CLUBS), new Card(Rank.THREE, Suit.CLUBS));
        player.bet(30);
        player.resetBetForNewRound();
        assertEquals(0, player.getCurrentBet());
        assertEquals(70, player.getChips());
        assertEquals(2, player.getHoleCards().size());
    }

    @Test
    void winChipsAddsToStack() {
        Player player = new Player("Alice", 100, false);
        player.winChips(50);
        assertEquals(150, player.getChips());
    }
}
