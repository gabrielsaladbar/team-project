package main.interface_adapters.view_stats;

import java.util.function.Consumer;
import use_case.view_stats.ViewStatsOutputBoundary;

public class ViewStatsPresenter implements ViewStatsOutputBoundary {
    private final Consumer<String> displayMessage;

    public ViewStatsPresenter(Consumer<String> displayMessage) {
        this.displayMessage = displayMessage;
    }

    @Override
    public void presentStats(int wins, int gamesPlayed) {
        if (gamesPlayed == 0) {
            displayMessage.accept("Play a game to see your statistics.");
            return;
        }

        long winPercentage = Math.round(100.0 * wins / gamesPlayed);
        String gameWord = gamesPlayed == 1 ? "game" : "games";

        displayMessage.accept(String.format(
                "You won %d of your last %d %s (%d%%).",
                wins, gamesPlayed, gameWord, winPercentage
        ));
    }

    @Override
    public void presentHistoryUnavailable() {
        displayMessage.accept("Couldn't load your game history. Please try again.");
    }
}
