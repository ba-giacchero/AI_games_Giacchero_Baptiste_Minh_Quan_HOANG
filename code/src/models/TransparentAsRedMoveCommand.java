package models;

import controllers.NormalSowStrategy;
import controllers.RuleController;
import controllers.SowStrategy;

public class TransparentAsRedMoveCommand extends AbstractMoveCommand {

    public TransparentAsRedMoveCommand(Board board,
                                       RuleController ruleController,
                                       Player currentPlayer,
                                       Player opponent,
                                       int holeIndex) {
        super(board, ruleController, currentPlayer, opponent, holeIndex);
    }

    public TransparentAsRedMoveCommand(Board board,
                                       RuleController ruleController,
                                       Player currentPlayer,
                                       Player opponent,
                                       int holeIndex,
                                       boolean silent) {
        super(board, ruleController, currentPlayer, opponent, holeIndex, silent);
    }

    @Override
    public boolean execute() {
        // Must own the hole
        if (!currentPlayer.ownsHoleIndex(holeIndex)) {
            System.out.println("Illegal move: that hole does not belong to you.");
            return false;
        }

        Hole startHole = board.getHole(holeIndex);
        int tSeeds = startHole.takeAllTransparent();
        int rSeeds = startHole.takeAllRed();

        if (tSeeds + rSeeds <= 0) {
            System.out.println("Illegal move: no transparent or red seeds in that hole.");
            return false;
        }

        SowStrategy redPattern = new NormalSowStrategy();
        int lastIndex = holeIndex;

        // Phase 1: transparent stones move first (as red pattern, still transparent)
        if (tSeeds > 0) {
            lastIndex = redPattern.sow(board, currentPlayer, lastIndex, tSeeds, SeedColor.TRANSPARENT);
        }

        // Phase 2: red stones move next from where transparents ended
        if (rSeeds > 0) {
            lastIndex = redPattern.sow(board, currentPlayer, lastIndex, rSeeds, SeedColor.RED);
        }

        if (!silent) {
            System.out.printf("%s plays Transparent-as-RED from hole %d; last seed lands in hole %d.%n",
                currentPlayer.getName(), holeIndex + 1, lastIndex + 1);
        }

        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);

        return true;
    }
}

