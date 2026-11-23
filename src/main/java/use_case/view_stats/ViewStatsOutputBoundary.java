package use_case.view_stats;

public interface ViewStatsOutputBoundary {
    void presentStats(int wins, int gamesPlayed);

    void presentHistoryUnavailable();
}
