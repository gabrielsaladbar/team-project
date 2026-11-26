package use_case.player_action;

import data_access.InMemoryGameDataAccessObject;
import entity.ActionType;
import entity.BotPersonality;
import entity.Card;
import entity.Game;
import entity.Player;
import entity.Rank;
import entity.Stage;
import entity.Suit;
import org.junit.jupiter.api.Test;
import use_case.CallingBotStrategy;
import use_case.start_hand.FakeStartHandPresenter;
import use_case.start_hand.StartHandInputData;
import use_case.start_hand.StartHandInteractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowdownTest {

    private final InMemoryGameDataAccessObject dataAccess = new InMemoryGameDataAccessObject();
    private final FakePlayerActionPresenter presenter = new FakePlayerActionPresenter();
    private final PlayerActionInteractor interactor =
            new PlayerActionInteractor(dataAccess, presenter, new CallingBotStrategy());

    /**
     * Turns a short name like "As" or "Td" into a card.
     */
    private Card card(String name) {
        Rank rank = null;
        for (Rank r : Rank.values()) {
            if (r.getSymbol() == name.charAt(0)) {
                rank = r;
            }
        }
        Suit suit = null;
        for (Suit s : Suit.values()) {
            if (s.getSymbol() == name.charAt(1)) {
                suit = s;
            }
        }
        return new Card(rank, suit);
    }

    /**
     * Sets up a game on the river with the given cards. Bot 2 deals, so Alice acts
     * first; the bots only check, so one check from Alice ends the hand.
     */
    private Game riverGame(String board, String alice, String bot1, String bot2, int pot) {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 1000, false));
        players.add(new Player("Bot 1", 1000, BotPersonality.BALANCED));
        players.add(new Player("Bot 2", 1000, BotPersonality.BALANCED));
        String[] hands = {alice, bot1, bot2};
        for (int i = 0; i < 3; i++) {
            String[] names = hands[i].split(" ");
            players.get(i).receiveCards(card(names[0]), card(names[1]));
        }

        Game game = new Game(players, 10, 20);
        for (String name : board.split(" ")) {
            game.addCommunityCard(card(name));
        }
        game.addToPot(pot);
        game.setDealerIndex(2);
        game.setCurrentPlayerIndex(0);
        game.setStage(Stage.RIVER);
        game.setPlayersLeftToAct(3);
        dataAccess.save(game);
        return game;
    }

    @Test
    void bestHandWinsThePot() {
        riverGame("2c 7d 9h Js Kd", "As Ah", "Kh Qc", "3s 4s", 300);
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals("HAND_OVER", output.getStage());
        assertEquals(Arrays.asList("Alice"), output.getWinnerNames());
        assertEquals("ONE_PAIR", output.getWinningHand());
        assertEquals(300, output.getAmountWon());
        assertEquals("WIN", output.getHumanResult());
        assertEquals(Arrays.asList(1300, 1000, 1000), output.getPlayerChips());
        assertEquals(0, output.getPot());
        // bots show their cards at showdown
        assertEquals(Arrays.asList("As Ah", "Kh Qc", "3s 4s"), output.getPlayerCards());
        assertTrue(output.getActionLog().contains("Alice wins 300 with one pair"));
    }

    @Test
    void botWithBetterHandBeatsHuman() {
        riverGame("2c 7d 9h Js Kd", "3c 4d", "9c 9d", "Ks 2h", 300);
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals(Arrays.asList("Bot 1"), output.getWinnerNames());
        assertEquals("THREE_OF_A_KIND", output.getWinningHand());
        assertEquals("LOSS", output.getHumanResult());
        assertEquals(Arrays.asList(1000, 1300, 1000), output.getPlayerChips());
    }

    @Test
    void tiedHandsSplitThePot() {
        // The board is a straight that nobody can beat, so all three tie.
        riverGame("As Ks Qd Jc Th", "2c 3d", "4h 5h", "6c 7c", 300);
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals(Arrays.asList("Alice", "Bot 1", "Bot 2"), output.getWinnerNames());
        assertEquals("STRAIGHT", output.getWinningHand());
        assertEquals(100, output.getAmountWon());
        assertEquals("TIE", output.getHumanResult());
        assertEquals(Arrays.asList(1100, 1100, 1100), output.getPlayerChips());
    }

    @Test
    void leftoverChipGoesToFirstWinnerAfterDealer() {
        Game game = riverGame("As Ks Qd Jc Th", "2c 3d", "4h 5h", "Ac Ad", 301);
        // Bot 2 folded, so it cannot win even with the best cards.
        game.getPlayers().get(2).fold();
        game.setPlayersLeftToAct(2);

        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals(Arrays.asList("Alice", "Bot 1"), output.getWinnerNames());
        assertEquals(150, output.getAmountWon());
        // Bot 2 is the dealer, so Alice is first after the dealer and gets the extra chip.
        assertEquals(Arrays.asList(1151, 1150, 1000), output.getPlayerChips());
        // a folded player's cards stay hidden
        assertEquals("?? ??", output.getPlayerCards().get(2));
    }

    @Test
    void fullHandReachesShowdownAndPaysTheBestHand() {
        StartHandInteractor startHand = new StartHandInteractor(dataAccess,
                new FakeStartHandPresenter(), new CallingBotStrategy(), new Random(5));
        startHand.execute(new StartHandInputData("Alice", 3, 1000, 10, 20));

        interactor.execute(new PlayerActionInputData(ActionType.CALL, 0));
        assertEquals("FLOP", presenter.getOutputData().getStage());
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));
        assertEquals("TURN", presenter.getOutputData().getStage());
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));
        assertEquals("RIVER", presenter.getOutputData().getStage());
        interactor.execute(new PlayerActionInputData(ActionType.CHECK, 0));

        PlayerActionOutputData output = presenter.getOutputData();
        assertEquals("HAND_OVER", output.getStage());
        assertTrue(output.isHandOver());
        assertFalse(output.isHumanTurn());
        assertEquals(5, output.getCommunityCards().size());
        assertEquals(0, output.getPot());
        assertFalse(output.getWinningHand().isEmpty());
        assertFalse(output.getHumanResult().isEmpty());

        // Every player put in 20, so the pot was 80; check it went to the best hand.
        Game game = dataAccess.getCurrentGame();
        List<Player> winners = game.getWinners();
        int total = 0;
        for (Player player : game.getPlayers()) {
            total = total + player.getChips();
            if (!winners.contains(player)) {
                assertEquals(980, player.getChips());
            }
        }
        assertEquals(4000, total);
        assertEquals(80 / winners.size(), output.getAmountWon());
    }
}
