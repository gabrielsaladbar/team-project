package use_case.view_stats;

import java.util.List;

public class ViewStatsDemo {
    public static void main(String[] args) {
        GameHistoryGateway sampleHistory = (playerId, limit) -> List.of(
                GameHistoryGateway.Outcome.WIN,
                GameHistoryGateway.Outcome.LOSS,
                GameHistoryGateway.Outcome.TIE
        );

        ViewStatsOutputBoundary consolePresenter = new ViewStatsOutputBoundary() {
            @Override
            public void presentStats(int wins, int gamesPlayed) {
                System.out.println("Wins: " + wins);
                System.out.println("Games played: " + gamesPlayed);
            }

            @Override
            public void presentHistoryUnavailable() {
                System.out.println("Could not load game history.");
            }
        };

        ViewStatsInteractor interactor =
                new ViewStatsInteractor(sampleHistory, consolePresenter);
        interactor.execute("player-1");
    }
}
