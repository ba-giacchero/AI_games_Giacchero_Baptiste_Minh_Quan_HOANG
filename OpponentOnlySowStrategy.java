public class OpponentOnlySowStrategy implements SowStrategy {
    @Override
    public int sow(Board board,
                   Player currentPlayer,
                   int startIndex,
                   int seeds,
                   SeedColor color) {
        if (seeds <= 0) return -1;

        int idx = startIndex;
        int lastIdx = -1;
        int n = board.getNumHoles();

        while (seeds > 0) {
            idx = (idx + 1) % n;
            if (idx == startIndex) continue;
            if (currentPlayer.ownsHoleIndex(idx)) continue;
            Hole h = board.getHole(idx);
            h.addSeed(color);
            seeds--;
            lastIdx = idx;
        }
        return lastIdx;
    }
}
