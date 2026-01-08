package models;

import controllers.OpponentOnlySowStrategy;
import controllers.RuleController;
import controllers.SowStrategy;

public class TransparentAsBlueMoveCommand extends AbstractMoveCommand {

    public TransparentAsBlueMoveCommand(Board board,
                                        RuleController ruleController,
                                        Player currentPlayer,
                                        Player opponent,
                                        int holeIndex) {
        super(board, ruleController, currentPlayer, opponent, holeIndex);
    }

    public TransparentAsBlueMoveCommand(Board board,
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
        int tSeeds = startHole.takeAllTransparent();
        int bSeeds = startHole.takeAllBlue();

        if (tSeeds + bSeeds <= 0) {
            System.out.println("Illegal move: no transparent or blue seeds in that hole.");
            return false;
        }

        SowStrategy bluePattern = new OpponentOnlySowStrategy();
        int lastIndex = holeIndex;

        // Phase 1: transparent stones with blue pattern
        if (tSeeds > 0) {
            lastIndex = bluePattern.sow(board, currentPlayer, lastIndex, tSeeds, SeedColor.TRANSPARENT);
        }

        // Phase 2: blue stones
        if (bSeeds > 0) {
            lastIndex = bluePattern.sow(board, currentPlayer, lastIndex, bSeeds, SeedColor.BLUE);
        }

        if (!silent) {
            System.out.printf("%s plays Transparent-as-BLUE from hole %d; last seed lands in hole %d.%n",
                currentPlayer.getName(), holeIndex + 1, lastIndex + 1);
        }

        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);

        return true;
    }
}

