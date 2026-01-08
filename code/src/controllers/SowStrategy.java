package controllers;

import models.Board;
import models.Player;
import models.SeedColor;

public interface SowStrategy {

    /**
     * Sow 'seeds' starting from startIndex, using rules depending on
     * the strategy and the current player.
     *
     * @param board        the board on which to sow
     * @param currentPlayer the player making the move
     * @param startIndex   index of the starting hole (0-based)
     * @param seeds        number of seeds to sow
     * @param color        color of the seeds to sow
     * @return index of the last hole that received a seed, or -1 if nothing was sown
     */
    int sow(Board board,
            Player currentPlayer,
            int startIndex,
            int seeds,
            SeedColor color);

}

