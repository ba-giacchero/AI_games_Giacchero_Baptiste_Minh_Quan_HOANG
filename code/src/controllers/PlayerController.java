package controllers;

import models.Board;
import models.Player;

public interface PlayerController {
    /**
     * Decide a move for the current player.
     * Returns a move text (e.g. "5R", "8B", "4TR", "7TB").
     */
    String chooseMove(Board board, Player[] players, int currentPlayerIndex);
}
