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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

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

    public void saveResult(String playerId, String gameId, long finishedAtMillis,
                           Outcome outcome) throws IOException {
        checkId(playerId);
        checkId(gameId);
        Objects.requireNonNull(outcome, "outcome");

        Path file = historyFile.toAbsolutePath();
        Files.createDirectories(file.getParent());
        Path lockFile = file.resolveSibling(file.getFileName() + ".lock");

        try (FileChannel channel = FileChannel.open(lockFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) {
                throw new IOException("Game history is busy. Try saving again.");
            }
            writeResult(file, playerId, gameId, finishedAtMillis, outcome);
        } catch (OverlappingFileLockException e) {
            throw new IOException("Game history is busy. Try saving again.", e);
        }
    }

    private static void writeResult(Path file, String playerId, String gameId,
                                    long finishedAtMillis, Outcome outcome) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (NoSuchFileException e) {
            lines = new ArrayList<>();
            lines.add(HEADER);
        }

        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
            throw new IOException("Game history has an invalid header.");
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] fields = lines.get(i).split(",", -1);
            if (fields.length != 4) {
                throw new IOException("Invalid game history row " + (i + 1));
            }
            if (fields[0].equals(playerId) && fields[1].equals(gameId)) {
                if (!fields[3].equals(outcome.name())) {
                    throw new IOException("A different outcome is already saved for this game.");
                }
                return;
            }
        }

        lines.add(String.join(",", playerId, gameId, Long.toString(finishedAtMillis),
                outcome.name()));

        Path temp = Files.createTempFile(file.getParent(), "game-history-", ".tmp");
        try {
            Files.write(temp, lines);
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void checkId(String id) {
        if (id == null || id.isBlank() || id.contains(",") || id.contains("\"")
                || id.contains("\n") || id.contains("\r")) {
            throw new IllegalArgumentException("Use a nonempty ID without CSV separators or quotes.");
        }
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
