package use_case.view_stats;

import java.util.List;
import main.interface_adapters.view_stats.ViewStatsPresenter;

import static use_case.view_stats.GameHistoryGateway.Outcome.LOSS;
import static use_case.view_stats.GameHistoryGateway.Outcome.TIE;
import static use_case.view_stats.GameHistoryGateway.Outcome.WIN;

public class ViewStatsDemo {
    public static void main(String[] args) {
        // Newest first. The two oldest games are both wins.
        List<GameHistoryGateway.Outcome> games = List.of(
                WIN, LOSS, TIE, WIN, LOSS, LOSS, WIN, LOSS, LOSS, LOSS,
                WIN, WIN);

        GameHistoryGateway history = (playerId, limit) ->
                games.subList(0, Math.min(limit, games.size()));

        ViewStatsPresenter presenter = new ViewStatsPresenter(System.out::println);
        new ViewStatsInteractor(history, presenter).execute("player-1");
    }
}