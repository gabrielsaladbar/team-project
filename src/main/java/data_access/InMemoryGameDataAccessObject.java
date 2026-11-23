package data_access;

import entity.Game;
import use_case.GameDataAccessInterface;

/**
 * Keeps the current game in memory. The game is lost when the program closes.
 */
public class InMemoryGameDataAccessObject implements GameDataAccessInterface {

    private Game currentGame = null;

    @Override
    public void save(Game game) {
        currentGame = game;
    }

    @Override
    public Game getCurrentGame() {
        return currentGame;
    }
}
