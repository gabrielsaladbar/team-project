package main.interface_adapters.view_stats;

import java.util.function.Consumer;
import use_case.view_stats.ViewStatsOutputBoundary;

public class ViewStatsPresenter implements ViewStatsOutputBoundary {
    private final Consumer<String> displayMessage;

    public ViewStatsPresenter(Consumer<String> displayMessage) {
        this.displayMessage = displayMessage;
    }

    @Override
    public void presentStats(int allWins, int allGames, int recentWins, int recentGames) {
        if (allGames == 0) {
            displayMessage.accept("Play a game to see your statistics.");
            return;
        }

        long allPercent = Math.round(100.0 * allWins / allGames);
        long recentPercent = Math.round(100.0 * recentWins / recentGames);

        displayMessage.accept(String.format(
                "All time %d/%d (%d%%) | Last %d %s %d/%d (%d%%)",
                allWins, allGames, allPercent,
                recentGames, recentGames == 1 ? "game" : "games",
                recentWins, recentGames, recentPercent));
    }

    @Override
    public void presentHistoryUnavailable() {
        displayMessage.accept("Couldn't load your game history. Please try again.");
    }
}
