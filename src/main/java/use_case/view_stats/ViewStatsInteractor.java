package use_case.view_stats;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

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
                    history.findRecentOutcomes(playerId, Integer.MAX_VALUE);

            int recentGames = Math.min(RECENT_GAME_LIMIT, games.size());
            int allWins = Collections.frequency(games, GameHistoryGateway.Outcome.WIN);
            int recentWins = Collections.frequency(
                    games.subList(0, recentGames), GameHistoryGateway.Outcome.WIN);

            presenter.presentStats(allWins, games.size(), recentWins, recentGames);
        } catch (IOException exception) {
            presenter.presentHistoryUnavailable();
        }
    }
}