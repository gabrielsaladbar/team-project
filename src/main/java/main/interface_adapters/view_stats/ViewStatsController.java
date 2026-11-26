package main.interface_adapters.view_stats;

import use_case.view_stats.ViewStatsInputBoundary;

public class ViewStatsController {
    private final ViewStatsInputBoundary interactor;

    public ViewStatsController(ViewStatsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void viewStats(String playerId) {
        interactor.execute(playerId);
    }
}
