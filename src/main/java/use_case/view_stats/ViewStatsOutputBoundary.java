package use_case.view_stats;

public interface ViewStatsOutputBoundary {
    void presentStats(int allWins, int allGames, int recentWins, int recentGames);

    void presentHistoryUnavailable();
}
