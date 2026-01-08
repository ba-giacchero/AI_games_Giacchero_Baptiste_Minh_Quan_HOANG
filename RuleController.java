public class RuleController {

    public void applyCaptures(Board board, Player current, int lastIndex) {
        int i = lastIndex;
        int n = board.getNumHoles();

        while (true) {
            Hole h = board.getHole(i);
            int count = h.total();
            if (count == 2 || count == 3) {
                current.addCaptured(count);
                h.clear();
                i = (i - 1 + n) % n;
            } else {
                break;
            }
        }
    }

    public void applyStarvation(Board board, Player current, Player opponent) {
        int oppSeeds = board.totalSeedsOwnedBy(opponent);
        if (oppSeeds == 0) {
            int remaining = board.collectAllSeeds();
            current.addCaptured(remaining);
        }
    }

    public boolean isGameOver(Board board, Player[] players) {
        int remaining = board.totalSeeds();
        int p1 = players[0].getCaptured();
        int p2 = players[1].getCaptured();

        if (p1 >= 49 || p2 >= 49) return true;
        return remaining < 10;
    }

    public int winner(Player[] players) {
        int p1 = players[0].getCaptured();
        int p2 = players[1].getCaptured();
        return Integer.compare(p1, p2);
    }
}
