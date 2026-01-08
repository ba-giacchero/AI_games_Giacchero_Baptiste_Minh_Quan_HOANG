package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import models.BlueMoveCommand;
import models.Board;
import models.Hole;
import models.MoveCommand;
import models.Player;
import models.RedMoveCommand;
import models.TransparentAsBlueMoveCommand;
import models.TransparentAsRedMoveCommand;

// IA basée sur minimax avec profondeur limitée.
// Utilise Evaluator pour évaluer les positions pour le joueur racine.
public class MinimaxPlayerController implements PlayerController {

    private final Evaluator evaluator;
    private final int maxDepth;
    private final long timeLimitMillis; // <= 0 signifie "pas de limite explicite"
    private final boolean verbose;
    private long deadlineNanos;

    // Limite de profondeur courante (utilisée pour l'itérative deepening)
    private int currentDepthLimit;

    // Générateur aléatoire pour départager plusieurs coups équivalents
    private final Random random = new Random();

    // Killer move par joueur et par profondeur (un seul killer simple par profondeur)
    private final MoveOption[][] killerMoves;

    // History heuristic : history[playerIndex][holeIndex][moveType]
    private final int[][][] history;

    public MinimaxPlayerController(Evaluator evaluator, int maxDepth, long timeLimitMillis) {
        this(evaluator, maxDepth, timeLimitMillis, true);
    }

    public MinimaxPlayerController(Evaluator evaluator, int maxDepth, long timeLimitMillis, boolean verbose) {
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
        this.timeLimitMillis = timeLimitMillis;
        this.verbose = verbose;

        // Allocation des structures pour killer moves et history heuristic
        this.killerMoves = new MoveOption[2][maxDepth + 1];
        this.history = new int[2][16][MoveType.values().length];
    }

    private enum MoveType { R, B, TR, TB }

    private static class MoveOption {
        final int holeIndex; // 0-based
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

    @Override
    public String chooseMove(Board board, Player[] players, int currentPlayerIndex) {
        // L'IA choisit un coup en supposant qu'elle est le joueur courant.
        int rootPlayerIndex = currentPlayerIndex;

        // initialisation de la deadline temps (si activée)
        if (timeLimitMillis > 0) {
            deadlineNanos = System.nanoTime() + timeLimitMillis * 1_000_000L;
        } else {
            deadlineNanos = 0L;
        }

        RuleController ruleController = new RuleController();
        if (ruleController.isGameOver(board, players)) {
            return null; // pas de coup si la partie est déjà finie
        }

        List<MoveOption> moves = generateLegalMoves(board, players[currentPlayerIndex]);
        if (moves.isEmpty()) {
            return null;
        }

        // Ordonne les coups pour la racine en utilisant killer/history
        orderMoves(board, moves, currentPlayerIndex, 0);
        // Itérative deepening : on augmente progressivement la profondeur de recherche
        double bestScoreOverall = Double.NEGATIVE_INFINITY;
        List<MoveOption> bestMovesOverall = new ArrayList<>();

        for (int depthLimit = 1; depthLimit <= maxDepth; depthLimit++) {
            currentDepthLimit = depthLimit;

            double bestScoreThisDepth = Double.NEGATIVE_INFINITY;
            List<MoveOption> bestMovesThisDepth = new ArrayList<>();

            double alpha = Double.NEGATIVE_INFINITY;
            double beta = Double.POSITIVE_INFINITY;

            for (MoveOption move : moves) {
                if (isTimeUp()) {
                    break;
                }

                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) {
                    continue;
                }

                double score = minimax(nextState.board,
                                       nextState.players,
                                       nextState.currentPlayerIndex,
                                       1,
                                       rootPlayerIndex,
                                       alpha,
                                       beta);

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
                // Pas de mise à jour killer/history ici : on ne coupe pas à ce niveau
            }

            // Si on a trouvé un meilleur score pour cette profondeur, on le conserve.
            if (!bestMovesThisDepth.isEmpty() && bestScoreThisDepth >= bestScoreOverall) {
                bestScoreOverall = bestScoreThisDepth;
                bestMovesOverall = new ArrayList<>(bestMovesThisDepth);
            }

            if (isTimeUp()) {
                break;
            }
        }

        // si aucune évaluation n'a été faite (par exemple timeout très court),
        // on choisit au moins le premier coup légal
        if (bestMovesOverall.isEmpty()) {
            bestMovesOverall.add(moves.get(0));
            bestScoreOverall = evaluator.evaluate(board, players, rootPlayerIndex);
        }

        // Choix aléatoire parmi les meilleurs coups (même score)
        MoveOption chosen = bestMovesOverall.get(random.nextInt(bestMovesOverall.size()));

        String notation = chosen.toNotation();
        if (verbose) {
            // Traces de debug sur stderr pour ne pas perturber les protocoles basés sur stdout
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

    private double minimax(Board board,
                            Player[] players,
                            int currentPlayerIndex,
                            int depth,
                            int rootPlayerIndex,
                            double alpha,
                            double beta) {
        // Coupure par temps : si on dépasse la limite, on renvoie simplement
        // l'évaluation statique de la position courante.
        if (timeLimitMillis > 0 && deadlineNanos > 0L) {
            if (System.nanoTime() > deadlineNanos) {
                int eval = evaluator.evaluate(board, players, rootPlayerIndex);
                return eval;
            }
        }

        RuleController ruleController = new RuleController();
        if (depth >= currentDepthLimit || ruleController.isGameOver(board, players)) {
            int eval = evaluator.evaluate(board, players, rootPlayerIndex);
            return eval;
        }

        boolean isMaximizing = (currentPlayerIndex == rootPlayerIndex);
        List<MoveOption> moves = generateLegalMoves(board, players[currentPlayerIndex]);
        if (moves.isEmpty()) {
            int eval = evaluator.evaluate(board, players, rootPlayerIndex);
            return eval;
        }

        // Ordonne les coups en fonction des killer moves et de l'historique
        orderMoves(board, moves, currentPlayerIndex, depth);

        if (isMaximizing) {
            double best = Double.NEGATIVE_INFINITY;
            for (MoveOption move : moves) {
                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) continue;

                double score = minimax(nextState.board,
                                       nextState.players,
                                       nextState.currentPlayerIndex,
                                       depth + 1,
                                       rootPlayerIndex,
                                       alpha,
                                       beta);

                if (score > best) {
                    best = score;
                }
                if (score > alpha) {
                    alpha = score;
                }
                if (beta <= alpha) {
                    // Beta cut : ce coup est un killer pour ce joueur/profondeur
                    storeKillerAndHistory(currentPlayerIndex, depth, move);
                    break; // coupe alpha-bêta
                }
            }
            return best;
        } else {
            double best = Double.POSITIVE_INFINITY;
            for (MoveOption move : moves) {
                NodeState nextState = applyMove(board, players, currentPlayerIndex, move);
                if (nextState == null) continue;

                double score = minimax(nextState.board,
                                       nextState.players,
                                       nextState.currentPlayerIndex,
                                       depth + 1,
                                       rootPlayerIndex,
                                       alpha,
                                       beta);

                if (score < best) {
                    best = score;
                }
                if (score < beta) {
                    beta = score;
                }
                if (beta <= alpha) {
                    // Beta cut côté minimisant
                    storeKillerAndHistory(currentPlayerIndex, depth, move);
                    break; // coupe alpha-bêta
                }
            }
            return best;
        }
    }

    private List<MoveOption> generateLegalMoves(Board board, Player currentPlayer) {
        List<MoveOption> moves = new ArrayList<>();

        int numHoles = board.getNumHoles();
        for (int i = 0; i < numHoles; i++) {
            if (!currentPlayer.ownsHoleIndex(i)) {
                continue;
            }

            Hole h = board.getHole(i);
            int red = h.getRed();
            int blue = h.getBlue();
            int transparent = h.getTransparent();

            if (red > 0) {
                moves.add(new MoveOption(i, MoveType.R));
            }
            if (blue > 0) {
                moves.add(new MoveOption(i, MoveType.B));
            }
            if (transparent + red > 0) {
                moves.add(new MoveOption(i, MoveType.TR));
            }
            if (transparent + blue > 0) {
                moves.add(new MoveOption(i, MoveType.TB));
            }
        }
        // Ordonnancement simple : on explore d'abord les coups joués depuis
        // les trous les plus chargés, ce qui améliore souvent l'élagage alpha-bêta.
        moves.sort((m1, m2) -> {
            int s1 = board.getHole(m1.holeIndex).total();
            int s2 = board.getHole(m2.holeIndex).total();
            return Integer.compare(s2, s1); // décroissant
        });

        return moves;
    }

    // Ordonne les coups selon : killer move d'abord, puis score history, puis nombre de graines
    private void orderMoves(Board board, List<MoveOption> moves, int playerIndex, int depth) {
        MoveOption killer = null;
        if (depth >= 0 && depth < killerMoves[playerIndex].length) {
            killer = killerMoves[playerIndex][depth];
        }

        MoveOption killerFinal = killer;
        moves.sort((m1, m2) -> {
            // 1) killer move en premier
            int k1 = isSameMove(m1, killerFinal) ? 1 : 0;
            int k2 = isSameMove(m2, killerFinal) ? 1 : 0;
            if (k1 != k2) {
                return Integer.compare(k2, k1); // killer d'abord
            }

            // 2) score history (descendant)
            int h1 = history[playerIndex][m1.holeIndex][m1.type.ordinal()];
            int h2 = history[playerIndex][m2.holeIndex][m2.type.ordinal()];
            if (h1 != h2) {
                return Integer.compare(h2, h1);
            }

            // 3) nombre de graines dans le trou (descendant)
            int s1 = board.getHole(m1.holeIndex).total();
            int s2 = board.getHole(m2.holeIndex).total();
            return Integer.compare(s2, s1);
        });
    }

    private boolean isSameMove(MoveOption a, MoveOption b) {
        if (a == null || b == null) return false;
        return a.holeIndex == b.holeIndex && a.type == b.type;
    }

    private void storeKillerAndHistory(int playerIndex, int depth, MoveOption move) {
        if (depth >= 0 && depth < killerMoves[playerIndex].length) {
            killerMoves[playerIndex][depth] = move;
        }

        int hole = move.holeIndex;
        if (hole < 0 || hole >= 16) {
            return;
        }
        int typeIdx = move.type.ordinal();
        // On renforce davantage les coups qui coupent profond
        int increment = depth * depth + 1;
        history[playerIndex][hole][typeIdx] += increment;
    }

    private boolean isTimeUp() {
        return timeLimitMillis > 0 && deadlineNanos > 0L && System.nanoTime() > deadlineNanos;
    }

    private NodeState applyMove(Board board,
                                Player[] players,
                                int currentPlayerIndex,
                                MoveOption move) {

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
