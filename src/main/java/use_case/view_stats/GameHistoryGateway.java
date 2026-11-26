package use_case.view_stats;

import java.io.IOException;
import java.util.List;

public interface GameHistoryGateway {
    enum Outcome {
        WIN, LOSS, TIE
    }

    /**
     * Returns up to limit completed games for this player, newest first.
     */
    List<Outcome> findRecentOutcomes(String playerId, int limit) throws IOException;
}
