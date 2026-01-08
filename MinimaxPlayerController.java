import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinimaxPlayerController {
    private final Evaluator evaluator;
    private final int maxDepth;
    private final long timeLimitMillis;
    private final boolean verbose;
    private long deadlineNanos;
    private int currentDepthLimit;
    private final Random random = new Random();
    private final MoveOption[][] killerMoves;
    private final int[][][] history;

    public MinimaxPlayerController(Evaluator evaluator, int maxDepth, long timeLimitMillis) {
        this(evaluator, maxDepth, timeLimitMillis, false);
    }

    public MinimaxPlayerController(Evaluator evaluator, int maxDepth, long timeLimitMillis, boolean verbose) {
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
        this.timeLimitMillis = timeLimitMillis;
        this.verbose = verbose;
        this.killerMoves = new MoveOption[2][maxDepth + 1];
        this.history = new int[2][16][MoveType.values().length];
    }

    private enum MoveType { R, B, TR, TB }

    private static class MoveOption {
        final int holeIndex;
        final MoveType type;
        MoveOption(int holeIndex, MoveType type) {
            this.holeIndex = holeIndex;
            this.type = type;
        }
        String toNotation() {
            int holeNumber = holeIndex + 1;
            return switch (type) {
                case R -> holeNumber + "R";
                case B -> holeNumber + "B";
                case TR -> holeNumber + "TR";
                case TB -> holeNumber + "TB";
            };
        }
    }

    public String chooseMove(Board board, Player[] players, int currentPlayerIndex) {
        int rootPlayerIndex = currentPlayerIndex;
        if (timeLimitMillis > 0) {
            deadlineNanos = System.nanoTime() + timeLimitMillis * 1_000_000L;
        } else {
            deadlineNanos = 0L;
        }

        RuleController ruleController = new RuleController();
        if (ruleController.isGameOver(board, players)) {
            return null;
        }

        List<MoveOption> moves = generateLegalMoves(board, players[currentPlayerIndex]);
        if (moves.isEmpty()) return null;

        orderMoves(board, moves, currentPlayerIndex, 0);
        double bestScoreOverall = Double.NEGATIVE_INFINITY;
        List<MoveOption> bestMovesOverall = new ArrayList<>();

        for (int depthLimit = 1; depthLimit <= maxDepth; depthLimit++) {
            currentDepthLimit = depthLimit;
            double bestScoreThisDepth = Double.NEGATIVE_INFINITY;
            List<MoveOption> bestMovesThisDepth = new ArrayList<>();
            double alpha = Double.NEGATIVE_INFINITY;
            double beta = Double.POSITIVE_INFINITY;

            for (MoveOption move : moves) {
                if (isTimeUp()) break;
                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) continue;
                double score = minimax(nextState.board, nextState.players, nextState.currentPlayerIndex,
                                       1, rootPlayerIndex, alpha, beta);
                if (score > bestScoreThisDepth) {
                    bestScoreThisDepth = score;
                    bestMovesThisDepth.clear();
                    bestMovesThisDepth.add(move);
                } else if (score == bestScoreThisDepth) {
                    bestMovesThisDepth.add(move);
                }
                if (score > alpha) {
                    alpha = score;
                }
            }

            if (!bestMovesThisDepth.isEmpty() && bestScoreThisDepth >= bestScoreOverall) {
                bestScoreOverall = bestScoreThisDepth;
                bestMovesOverall = new ArrayList<>(bestMovesThisDepth);
            }
            if (isTimeUp()) break;
        }

        if (bestMovesOverall.isEmpty()) {
            bestMovesOverall.add(moves.get(0));
            bestScoreOverall = evaluator.evaluate(board, players, rootPlayerIndex);
        }

        MoveOption chosen = bestMovesOverall.get(random.nextInt(bestMovesOverall.size()));
        String notation = chosen.toNotation();
        if (verbose) {
            System.err.printf("AI (player %d) chooses move %s (eval=%.1f)%n",
                    rootPlayerIndex + 1, notation, bestScoreOverall);
        }
        return notation;
    }

    private static class NodeState {
        final Board board;
        final Player[] players;
        final int currentPlayerIndex;
        NodeState(Board board, Player[] players, int currentPlayerIndex) {
            this.board = board;
            this.players = players;
            this.currentPlayerIndex = currentPlayerIndex;
        }
    }

    private double minimax(Board board, Player[] players, int currentPlayerIndex,
                            int depth, int rootPlayerIndex, double alpha, double beta) {
        if (timeLimitMillis > 0 && deadlineNanos > 0L && System.nanoTime() > deadlineNanos) {
            return evaluator.evaluate(board, players, rootPlayerIndex);
        }
        RuleController ruleController = new RuleController();
        if (depth >= currentDepthLimit || ruleController.isGameOver(board, players)) {
            return evaluator.evaluate(board, players, rootPlayerIndex);
        }

        boolean isMaximizing = (currentPlayerIndex == rootPlayerIndex);
        List<MoveOption> moves = generateLegalMoves(board, players[currentPlayerIndex]);
        if (moves.isEmpty()) return evaluator.evaluate(board, players, rootPlayerIndex);
        orderMoves(board, moves, currentPlayerIndex, depth);

        if (isMaximizing) {
            double best = Double.NEGATIVE_INFINITY;
            for (MoveOption move : moves) {
                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) continue;
                double score = minimax(nextState.board, nextState.players, nextState.currentPlayerIndex,
                                       depth + 1, rootPlayerIndex, alpha, beta);
                if (score > best) best = score;
                if (score > alpha) alpha = score;
                if (beta <= alpha) {
                    storeKillerAndHistory(currentPlayerIndex, depth, move);
                    break;
                }
            }
            return best;
        } else {
            double best = Double.POSITIVE_INFINITY;
            for (MoveOption move : moves) {
                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) continue;
                double score = minimax(nextState.board, nextState.players, nextState.currentPlayerIndex,
                                       depth + 1, rootPlayerIndex, alpha, beta);
                if (score < best) best = score;
                if (score < beta) beta = score;
                if (beta <= alpha) {
                    storeKillerAndHistory(currentPlayerIndex, depth, move);
                    break;
                }
            }
            return best;
        }
    }

    private List<MoveOption> generateLegalMoves(Board board, Player currentPlayer) {
        List<MoveOption> moves = new ArrayList<>();
        int numHoles = board.getNumHoles();
        for (int i = 0; i < numHoles; i++) {
            if (!currentPlayer.ownsHoleIndex(i)) continue;
            Hole h = board.getHole(i);
            int red = h.getRed();
            int blue = h.getBlue();
            int transparent = h.getTransparent();
            if (red > 0) moves.add(new MoveOption(i, MoveType.R));
            if (blue > 0) moves.add(new MoveOption(i, MoveType.B));
            if (transparent + red > 0) moves.add(new MoveOption(i, MoveType.TR));
            if (transparent + blue > 0) moves.add(new MoveOption(i, MoveType.TB));
        }
        moves.sort((m1, m2) -> {
            int s1 = board.getHole(m1.holeIndex).total();
            int s2 = board.getHole(m2.holeIndex).total();
            return Integer.compare(s2, s1);
        });
        return moves;
    }

    private void orderMoves(Board board, List<MoveOption> moves, int playerIndex, int depth) {
        MoveOption killer = null;
        if (depth >= 0 && depth < killerMoves[playerIndex].length) {
            killer = killerMoves[playerIndex][depth];
        }
        MoveOption killerFinal = killer;
        moves.sort((m1, m2) -> {
            int k1 = (killerFinal != null && m1.holeIndex == killerFinal.holeIndex && m1.type == killerFinal.type) ? 1 : 0;
            int k2 = (killerFinal != null && m2.holeIndex == killerFinal.holeIndex && m2.type == killerFinal.type) ? 1 : 0;
            if (k1 != k2) return Integer.compare(k2, k1);
            int h1 = history[playerIndex][m1.holeIndex][m1.type.ordinal()];
            int h2 = history[playerIndex][m2.holeIndex][m2.type.ordinal()];
            if (h1 != h2) return Integer.compare(h2, h1);
            int s1 = board.getHole(m1.holeIndex).total();
            int s2 = board.getHole(m2.holeIndex).total();
            return Integer.compare(s2, s1);
        });
    }

    private void storeKillerAndHistory(int playerIndex, int depth, MoveOption move) {
        if (depth >= 0 && depth < killerMoves[playerIndex].length) {
            killerMoves[playerIndex][depth] = move;
        }
        int hole = move.holeIndex;
        if (hole < 0 || hole >= 16) return;
        int typeIdx = move.type.ordinal();
        int increment = depth * depth + 1;
        history[playerIndex][hole][typeIdx] += increment;
    }

    private boolean isTimeUp() {
        return timeLimitMillis > 0 && deadlineNanos > 0L && System.nanoTime() > deadlineNanos;
    }

    private NodeState applyMove(Board board, Player[] players, int currentPlayerIndex, MoveOption move) {
        Board clonedBoard = new Board(board);
        Player[] clonedPlayers = new Player[players.length];
        for (int i = 0; i < players.length; i++) {
            clonedPlayers[i] = new Player(players[i]);
        }
        RuleController ruleController = new RuleController();
        Player current = clonedPlayers[currentPlayerIndex];
        Player opponent = clonedPlayers[1 - currentPlayerIndex];
        MoveCommand cmd;
        switch (move.type) {
            case R -> cmd = new RedMoveCommand(clonedBoard, ruleController, current, opponent, move.holeIndex, true);
            case B -> cmd = new BlueMoveCommand(clonedBoard, ruleController, current, opponent, move.holeIndex, true);
            case TR -> cmd = new TransparentAsRedMoveCommand(clonedBoard, ruleController, current, opponent, move.holeIndex, true);
            case TB -> cmd = new TransparentAsBlueMoveCommand(clonedBoard, ruleController, current, opponent, move.holeIndex, true);
            default -> throw new IllegalStateException("Unknown move type");
        }
        boolean ok = cmd.execute();
        if (!ok) {
            return null;
        }
        int nextPlayerIndex = 1 - currentPlayerIndex;
        return new NodeState(clonedBoard, clonedPlayers, nextPlayerIndex);
    }
}
