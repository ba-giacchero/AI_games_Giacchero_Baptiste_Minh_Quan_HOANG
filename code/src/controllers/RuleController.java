package controllers;

import models.Board;
import models.Hole;
import models.Player;

public class RuleController {

    /**
     * Capture rule:
     * Starting from lastIndex, go backwards while each hole has 2 or 3 seeds.
     * For each such hole: take all seeds and add to current player.
     */
    public void applyCaptures(Board board, Player current, int lastIndex) {
        int i = lastIndex;
        int n = board.getNumHoles();

        while (true) {
            Hole h = board.getHole(i);
            int count = h.total();
            if (count == 2 || count == 3) {
                current.addCaptured(count);
                h.clear();
                // move to previous hole (circular)
                i = (i - 1 + n) % n;
            } else {
                break;
            }
        }
    }

    /**
     * Starvation rule:
     * If opponent has no seeds on their side of the board,
     * current player captures ALL remaining seeds on the board.
     */
    public void applyStarvation(Board board, Player current, Player opponent) {
        int oppSeeds = board.totalSeedsOwnedBy(opponent);
        if (oppSeeds == 0) {
            int remaining = board.collectAllSeeds();
            current.addCaptured(remaining);
        }
    }

    // ----- new methods for full game loop -----

    /**
     * Returns true if the game is over, based on:
     * - any player has >= 49 captured seeds
     * - remaining seeds on board < 10 (in that case, remaining seeds are ignored)
     */
    public boolean isGameOver(Board board, Player[] players) {
        int remaining = board.totalSeeds();
        int p1 = players[0].getCaptured();
        int p2 = players[1].getCaptured();

        if (p1 >= 49 || p2 >= 49) return true;
        if (remaining < 10) return true;

        return false;
    }

    /**
     * Who wins?
     * @return 0 for Player 1, 1 for Player 2, -1 for draw.
     * Should only be called when isGameOver(...) == true.
     */
    public int winner(Player[] players) {
        int p1 = players[0].getCaptured();
        int p2 = players[1].getCaptured();
        return Integer.compare(p1, p2); // 1 if p2>p1, -1 if p1>p2, 0 if equal
    }
}
