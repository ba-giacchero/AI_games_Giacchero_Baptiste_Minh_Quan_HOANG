package models;

import controllers.OpponentOnlySowStrategy;
import controllers.RuleController;
import controllers.SowStrategy;

public class BlueMoveCommand extends AbstractMoveCommand {

    public BlueMoveCommand(Board board,
                           RuleController ruleController,
                           Player currentPlayer,
                           Player opponent,
                           int holeIndex) {
        super(board, ruleController, currentPlayer, opponent, holeIndex);
    }

    public BlueMoveCommand(Board board,
                           RuleController ruleController,
                           Player currentPlayer,
                           Player opponent,
                           int holeIndex,
                           boolean silent) {
        super(board, ruleController, currentPlayer, opponent, holeIndex, silent);
    }

    @Override
    public boolean execute() {
        if (!currentPlayer.ownsHoleIndex(holeIndex)) {
            System.out.println("Illegal move: that hole does not belong to you.");
            return false;
        }

        Hole startHole = board.getHole(holeIndex);
        int seeds = startHole.takeAllBlue();
        if (seeds <= 0) {
            System.out.println("Illegal move: no blue seeds in that hole.");
            return false;
        }

        SowStrategy strategy = new OpponentOnlySowStrategy();
        int lastIndex = strategy.sow(board, currentPlayer, holeIndex, seeds, SeedColor.BLUE);

        if (!silent) {
            System.out.printf("%s plays BLUE from hole %d, last seed lands in hole %d.%n",
                currentPlayer.getName(), holeIndex + 1, lastIndex + 1);
        }

        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);

        return true;
    }
}

