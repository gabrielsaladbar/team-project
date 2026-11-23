package data_access;

/**
 * game-history.csv Format:
 * playerId,gameId,finishedAtMillis,outcome
 * player-1,game-001,1795380000000,WIN
 * player-1,game-002,1795380300000,LOSS
 */

import use_case.view_stats.GameHistoryGateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileGameHistoryGateway implements GameHistoryGateway {
    private static final String HEADER = "playerId,gameId,finishedAtMillis,outcome";
    private final Path historyFile;

    public FileGameHistoryGateway(Path historyFile) {
        this.historyFile = historyFile;
    }

    @Override
    public List<Outcome> findRecentOutcomes(String playerId, int limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be zero or greater.");
        }

        List<GameResult> games = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(historyFile)) {
            if (!HEADER.equals(reader.readLine())) {
                throw new IOException("Game history has an invalid header.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 4) {
                    throw new IOException("A game history row must have four fields.");
                }

                if (fields[0].equals(playerId)) {
                    long finishedAt = Long.parseLong(fields[2]);
                    Outcome outcome = Outcome.valueOf(fields[3]);
                    games.add(new GameResult(finishedAt, outcome));
                }
            }
        } catch (NoSuchFileException exception) {
            return List.of();
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid timestamp or outcome in game history.", exception);
        }

        games.sort((first, second) -> Long.compare(second.finishedAt, first.finishedAt));

        List<Outcome> outcomes = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, games.size()); i++) {
            outcomes.add(games.get(i).outcome);
        }
        return outcomes;
    }

    private static class GameResult {
        private final long finishedAt;
        private final Outcome outcome;

        GameResult(long finishedAt, Outcome outcome) {
            this.finishedAt = finishedAt;
            this.outcome = outcome;
        }
    }
}
