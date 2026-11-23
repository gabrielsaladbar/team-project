package data_access;

import entity.Game;
import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class InMemoryGameDataAccessObjectTest {

    @Test
    void startsWithNoGame() {
        assertNull(new InMemoryGameDataAccessObject().getCurrentGame());
    }

    @Test
    void returnsSavedGame() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 1000, false));
        Game game = new Game(players, 10, 20);

        InMemoryGameDataAccessObject dataAccess = new InMemoryGameDataAccessObject();
        dataAccess.save(game);
        assertSame(game, dataAccess.getCurrentGame());
    }
}
