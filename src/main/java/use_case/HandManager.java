package use_case;

import entity.ActionType;
import entity.BotDecision;
import entity.BotStrategy;
import entity.Card;
import entity.Game;
import entity.Player;
import entity.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a hand once it has been dealt: applies moves, lets the bots take their turns,
 * and moves the hand from one stage to the next. Shared by the start hand and
 * player action interactors so this logic is only written once.
 */
public class HandManager {

    private final BotStrategy botStrategy;

    /**
     * Creates a hand manager.
     *
     * @param botStrategy decides what each bot does on its turn
     */
    public HandManager(BotStrategy botStrategy) {
        this.botStrategy = botStrategy;
    }

    /**
     * Lets the bots act, in turn order, until it is the human's turn or the hand ends.
     * Finished betting rounds are closed and the next cards are dealt along the way.
     *
     * @param game the game to play
     */
    public void playUntilHumanTurn(Game game) {
        while (isBettingStage(game.getStage())) {
            if (countPlayersInHand(game) == 1) {
                awardPotToLastPlayer(game);
                return;
            }
            if (isRoundComplete(game)) {
                advanceStage(game);
            } else {
                Player current = game.getCurrentPlayer();
                if (!current.isBot()) {
                    return;
                }
                BotDecision decision = botStrategy.decide(game, current);
                takeTurn(game, decision.getType(), decision.getAmount());
            }
        }
    }

    /**
     * Applies a move for the player whose turn it is, then passes the turn on.
     * The move is assumed to be legal.
     *
     * @param game the game being played
     * @param type the move
     * @param raiseTo for a raise, the total bet to raise to; ignored otherwise
     */
    public void takeTurn(Game game, ActionType type, int raiseTo) {
        Player player = game.getCurrentPlayer();
        String name = player.getName();

        if (type == ActionType.FOLD) {
            player.fold();
            game.addToLog(name + " folds");
            game.setPlayersLeftToAct(game.getPlayersLeftToAct() - 1);
        } else if (type == ActionType.CHECK) {
            game.addToLog(name + " checks");
            game.setPlayersLeftToAct(game.getPlayersLeftToAct() - 1);
        } else if (type == ActionType.CALL) {
            int amount = Math.min(game.getAmountToCall(player), player.getChips());
            player.bet(amount);
            game.addToLog(name + " calls " + amount + allInText(player));
            game.setPlayersLeftToAct(game.getPlayersLeftToAct() - 1);
        } else {
            player.bet(raiseTo - player.getCurrentBet());
            game.setCurrentHighestBet(raiseTo);
            game.addToLog(name + " raises to " + raiseTo + allInText(player));
            // After a raise, everyone else who can still bet has to act again.
            int othersToAct = countPlayersAbleToAct(game);
            if (canAct(player)) {
                othersToAct = othersToAct - 1;
            }
            game.setPlayersLeftToAct(othersToAct);
        }

        game.setCurrentPlayerIndex(nextPlayerAbleToAct(game, game.getCurrentPlayerIndex()));
    }

    /**
     * Starts a betting round: the first player after the given seat who can still bet
     * goes first, and everyone who can still bet must act once.
     *
     * @param game the game being played
     * @param seatBeforeFirst the seat just before the player who should act first
     */
    public void startBettingRound(Game game, int seatBeforeFirst) {
        game.setCurrentPlayerIndex(nextPlayerAbleToAct(game, seatBeforeFirst));
        game.setPlayersLeftToAct(countPlayersAbleToAct(game));
    }

    /**
     * Returns the moves the human may make right now, or an empty list if it is not
     * the human's turn.
     *
     * @param game the game being played
     * @return the legal actions for the human
     */
    public List<ActionType> getHumanLegalActions(Game game) {
        List<ActionType> actions = new ArrayList<>();
        Player human = game.getHumanPlayer();
        if (!isBettingStage(game.getStage()) || game.getCurrentPlayer() != human || !canAct(human)) {
            return actions;
        }
        actions.add(ActionType.FOLD);
        if (game.getAmountToCall(human) == 0) {
            actions.add(ActionType.CHECK);
        } else {
            actions.add(ActionType.CALL);
        }
        if (human.getChips() + human.getCurrentBet() >= game.getMinimumRaiseTo()) {
            actions.add(ActionType.RAISE);
        }
        return actions;
    }

    /**
     * A player can bet if they have not folded and still have chips.
     *
     * @param player the player to check
     * @return true if the player can still bet this hand
     */
    public boolean canAct(Player player) {
        return !player.isFolded() && !player.isAllIn();
    }

    private int countPlayersAbleToAct(Game game) {
        int count = 0;
        for (Player player : game.getPlayers()) {
            if (canAct(player)) {
                count++;
            }
        }
        return count;
    }

    private int countPlayersInHand(Game game) {
        int count = 0;
        for (Player player : game.getPlayers()) {
            if (!player.isFolded()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the next seat after fromIndex whose player can still bet, going around
     * the table. Returns fromIndex if nobody can.
     */
    private int nextPlayerAbleToAct(Game game, int fromIndex) {
        List<Player> players = game.getPlayers();
        for (int step = 1; step <= players.size(); step++) {
            int index = (fromIndex + step) % players.size();
            if (canAct(players.get(index))) {
                return index;
            }
        }
        return fromIndex;
    }

    /**
     * A round is over when everyone who can bet has acted since the last raise.
     * It is also over when nobody, or only one player, can still bet and there is
     * nothing left for them to call.
     */
    private boolean isRoundComplete(Game game) {
        int ableToAct = countPlayersAbleToAct(game);
        if (ableToAct == 0) {
            return true;
        }
        if (ableToAct == 1) {
            Player onlyBettor = game.getPlayers().get(nextPlayerAbleToAct(game, game.getCurrentPlayerIndex()));
            if (game.getAmountToCall(onlyBettor) == 0) {
                return true;
            }
        }
        return game.getPlayersLeftToAct() <= 0;
    }

    /**
     * Moves the bets into the pot and goes to the next stage, dealing the flop, turn
     * or river.
     */
    private void advanceStage(Game game) {
        collectBets(game);
        Stage stage = game.getStage();
        if (stage == Stage.PRE_FLOP) {
            dealCommunityCards(game, 3, "Flop");
            game.setStage(Stage.FLOP);
        } else if (stage == Stage.FLOP) {
            dealCommunityCards(game, 1, "Turn");
            game.setStage(Stage.TURN);
        } else if (stage == Stage.TURN) {
            dealCommunityCards(game, 1, "River");
            game.setStage(Stage.RIVER);
        } else {
            game.setStage(Stage.SHOWDOWN);
            return;
        }
        startBettingRound(game, game.getDealerIndex());
    }

    private void collectBets(Game game) {
        for (Player player : game.getPlayers()) {
            game.addToPot(player.getCurrentBet());
            player.resetBetForNewRound();
        }
        game.setCurrentHighestBet(0);
    }

    private void dealCommunityCards(Game game, int count, String stageName) {
        String message = stageName + ":";
        for (int i = 0; i < count; i++) {
            Card card = game.getDeck().deal();
            game.addCommunityCard(card);
            message = message + " " + card;
        }
        game.addToLog(message);
    }

    /**
     * Everyone else folded, so the last player in the hand wins the whole pot.
     */
    private void awardPotToLastPlayer(Game game) {
        collectBets(game);
        Player winner = null;
        for (Player player : game.getPlayers()) {
            if (!player.isFolded()) {
                winner = player;
            }
        }
        int amount = game.getPot();
        winner.winChips(amount);
        List<Player> winners = new ArrayList<>();
        winners.add(winner);
        game.setHandResult(winners, null, amount);
        game.addToLog(winner.getName() + " wins " + amount);
        game.clearPot();
        game.setStage(Stage.HAND_OVER);
    }

    private boolean isBettingStage(Stage stage) {
        return stage == Stage.PRE_FLOP || stage == Stage.FLOP
                || stage == Stage.TURN || stage == Stage.RIVER;
    }

    private String allInText(Player player) {
        if (player.isAllIn()) {
            return " and is all in";
        }
        return "";
    }
}
