package use_case.hand_odds;

import java.io.IOException;

public interface HandOddsGateway {

    /**
     * Returns the probability of winning outright, from 0.0 to 1.0.
     *
     * @param holeCards the player's two cards, for example "AsKs"
     * @param board the visible community cards, or "" before the flop
     * @param opponents the number of opponents still in the hand, from 1 to 5
     */
    double getWinProbability(String holeCards, String board, int opponents)
            throws IOException, InterruptedException;
}
