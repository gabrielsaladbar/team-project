package entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleBotStrategyTest {

    private Game makeGame(Player bot) {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 1000, false));
        players.add(bot);
        return new Game(players, 10, 20);
    }

    @Test
    void onlyReturnsLegalActions() {
        BotPersonality[] personalities = BotPersonality.values();
        int[] highestBets = {0, 20, 40, 100, 500};
        int[] communitySizes = {0, 3, 4, 5};

        for (int seed = 0; seed < 200; seed++) {
            Random random = new Random(seed);
            Deck deck = new Deck(random);
            deck.shuffle();

            int startingChips = 20 + random.nextInt(400);
            Player bot = new Player("Bot", startingChips, personalities[seed % personalities.length]);
            bot.receiveCards(deck.deal(), deck.deal());
            Game game = makeGame(bot);

            int highestBet = highestBets[seed % highestBets.length];
            game.setCurrentHighestBet(highestBet);
            bot.bet(Math.min(highestBet / 2, startingChips - 1));

            int communitySize = communitySizes[seed % communitySizes.length];
            for (int i = 0; i < communitySize; i++) {
                game.addCommunityCard(deck.deal());
            }

            SimpleBotStrategy strategy = new SimpleBotStrategy(new Random(seed));
            BotDecision decision = strategy.decide(game, bot);
            int toCall = game.getAmountToCall(bot);

            if (decision.getType() == ActionType.CHECK) {
                assertEquals(0, toCall, "checked with a bet to call");
            } else if (decision.getType() == ActionType.CALL) {
                assertTrue(toCall > 0, "called with nothing to call");
            } else if (decision.getType() == ActionType.FOLD) {
                assertTrue(toCall > 0, "folded when checking was free");
            } else {
                assertTrue(decision.getAmount() >= game.getMinimumRaiseTo(), "raise below minimum");
                assertTrue(decision.getAmount() - bot.getCurrentBet() <= bot.getChips(),
                        "raised more chips than the bot has");
            }
        }
    }

    @Test
    void raisesWithPocketAces() {
        Player bot = new Player("Bot", 1000, BotPersonality.CAUTIOUS);
        bot.receiveCards(new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS));
        Game game = makeGame(bot);
        game.setCurrentHighestBet(20);

        BotDecision decision = new SimpleBotStrategy(new Random(1)).decide(game, bot);
        assertEquals(ActionType.RAISE, decision.getType());
        assertEquals(40, decision.getAmount());
    }

    @Test
    void cautiousBotFoldsJunkFacingABet() {
        Player bot = new Player("Bot", 1000, BotPersonality.CAUTIOUS);
        bot.receiveCards(new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS));
        Game game = makeGame(bot);
        game.setCurrentHighestBet(20);

        BotDecision decision = new SimpleBotStrategy(new Random(1)).decide(game, bot);
        assertEquals(ActionType.FOLD, decision.getType());
    }

    @Test
    void checksJunkWhenNothingToCall() {
        Player bot = new Player("Bot", 1000, BotPersonality.CAUTIOUS);
        bot.receiveCards(new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS));
        Game game = makeGame(bot);

        BotDecision decision = new SimpleBotStrategy(new Random(1)).decide(game, bot);
        assertEquals(ActionType.CHECK, decision.getType());
    }

    @Test
    void usesBoardAfterFlop() {
        Player bot = new Player("Bot", 1000, BotPersonality.CAUTIOUS);
        bot.receiveCards(new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS));
        Game game = makeGame(bot);
        game.addCommunityCard(new Card(Rank.SEVEN, Suit.HEARTS));
        game.addCommunityCard(new Card(Rank.SEVEN, Suit.CLUBS));
        game.addCommunityCard(new Card(Rank.KING, Suit.DIAMONDS));
        game.setCurrentHighestBet(20);

        // three sevens is strong enough for even a cautious bot to raise
        BotDecision decision = new SimpleBotStrategy(new Random(1)).decide(game, bot);
        assertEquals(ActionType.RAISE, decision.getType());
    }

    @Test
    void doesNotRaiseWithoutEnoughChips() {
        Player bot = new Player("Bot", 30, BotPersonality.AGGRESSIVE);
        bot.receiveCards(new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS));
        Game game = makeGame(bot);
        game.setCurrentHighestBet(20);

        BotDecision decision = new SimpleBotStrategy(new Random(1)).decide(game, bot);
        assertEquals(ActionType.CALL, decision.getType());
    }
}
