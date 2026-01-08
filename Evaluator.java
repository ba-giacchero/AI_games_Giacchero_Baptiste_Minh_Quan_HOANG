public class Evaluator {
    private final double capturedWeight;
    private final double boardWeight;
    private final double scale;
    private final double famineWeight;
    private static final double MOBILITY_WEIGHT = 0.2;

    public Evaluator() {
        this(1.0, 0.5, 0.5, 0.3);
    }

    public Evaluator(double capturedWeight, double boardWeight, double scale) {
        this(capturedWeight, boardWeight, scale, 0.3);
    }

    public Evaluator(double capturedWeight, double boardWeight, double scale, double famineWeight) {
        this.capturedWeight = capturedWeight;
        this.boardWeight = boardWeight;
        this.scale = scale;
        this.famineWeight = famineWeight;
    }

    public int evaluate(Board board, Player[] players, int id_idx) {
        Player me = players[id_idx];
        Player opp = players[1 - id_idx];

        int myCaptured = me.getCaptured();
        int oppCaptured = opp.getCaptured();
        int myBoardSeeds = board.totalSeedsOwnedBy(me);
        int oppBoardSeeds = board.totalSeedsOwnedBy(opp);

        double myMaterial = capturedWeight * myCaptured + boardWeight * myBoardSeeds;
        double oppMaterial = capturedWeight * oppCaptured + boardWeight * oppBoardSeeds;
        double diff = myMaterial - oppMaterial;

        double famineDiff = myBoardSeeds - oppBoardSeeds;
        diff += famineWeight * famineDiff;

        int myMobility = approximateMoveCount(board, me);
        int oppMobility = approximateMoveCount(board, opp);
        double mobilityDiff = myMobility - oppMobility;
        diff += MOBILITY_WEIGHT * mobilityDiff;

        double rawScore = 50.0 + scale * diff;
        if (rawScore < 0.0) rawScore = 0.0;
        if (rawScore > 100.0) rawScore = 100.0;
        return (int) Math.round(rawScore);
    }

    private int approximateMoveCount(Board board, Player player) {
        int count = 0;
        int numHoles = board.getNumHoles();
        for (int i = 0; i < numHoles; i++) {
            if (!player.ownsHoleIndex(i)) continue;
            Hole h = board.getHole(i);
            int red = h.getRed();
            int blue = h.getBlue();
            int transparent = h.getTransparent();
            if (red > 0) count++;
            if (blue > 0) count++;
            if (transparent + red > 0) count++;
            if (transparent + blue > 0) count++;
        }
        return count;
    }
}
