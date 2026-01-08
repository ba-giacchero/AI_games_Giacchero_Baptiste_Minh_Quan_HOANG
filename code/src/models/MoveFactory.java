package models;

import controllers.RuleController;

public class MoveFactory {

    private final Board board;
    private final RuleController ruleController;
    private final Player[] players;
    private final boolean silent;

    public MoveFactory(Board board,
                       RuleController ruleController,
                       Player[] players) {
        this(board, ruleController, players, false);
    }

    public MoveFactory(Board board,
                       RuleController ruleController,
                       Player[] players,
                       boolean silent) {
        this.board = board;
        this.ruleController = ruleController;
        this.players = players;
        this.silent = silent;
    }

    public MoveCommand createMove(String input, Player currentPlayer) {
        if (input == null) return null;

        String s = input.trim().toUpperCase();
        if (s.isEmpty()) return null;

        int i = 0;
        int len = s.length();
        int holeNumber = 0;

        // read digits for hole number
        while (i < len && Character.isDigit(s.charAt(i))) {
            holeNumber = holeNumber * 10 + (s.charAt(i) - '0');
            i++;
        }

        if (holeNumber < 1 || holeNumber > board.getNumHoles()) {
            // Utiliser stderr pour ne pas perturber les protocoles basés sur stdout
            System.err.println("Invalid hole number.");
            return null;
        }
        int holeIndex = holeNumber - 1;

        // remaining part is the move type (like "R", "B", "TR", "TB")
        String type = s.substring(i).replaceAll("\\s+", ""); // remove any spaces

        if (type.isEmpty()) {
            System.err.println("Missing move type (R, B, TR, or TB).");
            return null;
        }

        Player opponent = players[1 - currentPlayer.getIndex()];

        return switch (type) {
            case "R" -> silent
                ? new RedMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex, true)
                : new RedMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex);
            case "B" -> silent
                ? new BlueMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex, true)
                : new BlueMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex);
            case "TR" -> silent
                ? new TransparentAsRedMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex, true)
                : new TransparentAsRedMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex);
            case "TB" -> silent
                ? new TransparentAsBlueMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex, true)
                : new TransparentAsBlueMoveCommand(board, ruleController, currentPlayer, opponent, holeIndex);
            default -> {
            System.err.println("Unknown move type: " + type + " (expected R, B, TR, or TB).");
            yield null;
            }
        };
    }
}


