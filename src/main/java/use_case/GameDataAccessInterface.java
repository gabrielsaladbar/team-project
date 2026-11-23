package use_case;

import entity.Game;

/**
 * Stores and retrieves the game that is currently being played.
 */
public interface GameDataAccessInterface {

    /**
     * Saves the given game as the current game.
     *
     * @param game the game to save
     */
    void save(Game game);

    /**
     * Returns the current game.
     *
     * @return the saved game, or null if no game has been saved yet
     */
    Game getCurrentGame();
}
