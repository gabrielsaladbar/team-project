package use_case.view_stats;

import java.io.IOException;
import java.util.List;

public class ViewStatsInteractor implements ViewStatsInputBoundary{
    private static final int RECENT_GAME_LIMIT = 10;

    private final GameHistoryGateway history;
    private final ViewStatsOutputBoundary presenter;

    public ViewStatsInteractor(GameHistoryGateway history,
                               ViewStatsOutputBoundary presenter) {
        this.history = history;
        this.presenter = presenter;
    }

    @Override
    public void execute(String playerId) {
        try {
            List<GameHistoryGateway.Outcome> games =
                    history.findRecentOutcomes(playerId, RECENT_GAME_LIMIT);

            int wins = 0;
            for (GameHistoryGateway.Outcome game : games) {
                if (game == GameHistoryGateway.Outcome.WIN) {
                    wins++;
                }
            }

            presenter.presentStats(wins, games.size());
        } catch (IOException exception) {
            presenter.presentHistoryUnavailable();
        }
    }
}