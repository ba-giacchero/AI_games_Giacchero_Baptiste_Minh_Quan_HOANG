public class PhaseEvaluator extends Evaluator {
    private final Evaluator openingEval;
    private final Evaluator midEval;
    private final Evaluator endEval;
    private final int openingThreshold;
    private final int midThreshold;

    public enum PhaseProfile {
        BALANCED,
        AGGRESSIVE_ENDGAME,
        SAFE_OPENING,
        ULTIMATE
    }

    public PhaseEvaluator() {
        this(PhaseProfile.BALANCED);
    }

    public PhaseEvaluator(PhaseProfile profile) {
        super(1.0, 0.7, 0.5, 0.3);
        switch (profile) {
            case BALANCED -> {
                this.openingEval = new Evaluator(0.8, 0.9, 0.5, 0.3);
                this.midEval = new Evaluator(1.0, 0.7, 0.5, 0.3);
                this.endEval = new Evaluator(1.3, 0.5, 0.5, 0.3);
                this.openingThreshold = 70;
                this.midThreshold = 40;
            }
            case AGGRESSIVE_ENDGAME -> {
                this.openingEval = new Evaluator(0.9, 0.8, 0.5, 0.3);
                this.midEval = new Evaluator(1.1, 0.7, 0.5, 0.3);
                this.endEval = new Evaluator(1.6, 0.4, 0.5, 0.3);
                this.openingThreshold = 70;
                this.midThreshold = 45;
            }
            case SAFE_OPENING -> {
                this.openingEval = new Evaluator(0.7, 1.0, 0.5, 0.25);
                this.midEval = new Evaluator(1.0, 0.7, 0.5, 0.3);
                this.endEval = new Evaluator(1.4, 0.5, 0.5, 0.3);
                this.openingThreshold = 75;
                this.midThreshold = 40;
            }
            case ULTIMATE -> {
                this.openingEval = new Evaluator(0.7, 1.0, 0.5, 0.25);
                this.midEval = new Evaluator(1.2, 0.6, 0.5, 0.35);
                this.endEval = new Evaluator(1.6, 0.4, 0.5, 0.3);
                this.openingThreshold = 75;
                this.midThreshold = 45;
            }
            default -> throw new IllegalArgumentException("Unknown phase profile: " + profile);
        }
    }

    @Override
    public int evaluate(Board board, Player[] players, int id_idx) {
        int total = board.totalSeeds();
        if (total > openingThreshold) {
            return openingEval.evaluate(board, players, id_idx);
        } else if (total > midThreshold) {
            return midEval.evaluate(board, players, id_idx);
        } else {
            return endEval.evaluate(board, players, id_idx);
        }
    }
}
