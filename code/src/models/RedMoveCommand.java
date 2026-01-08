package models;

import controllers.NormalSowStrategy;
import controllers.RuleController;
import controllers.SowStrategy;

public class RedMoveCommand extends AbstractMoveCommand {

    public RedMoveCommand(Board board,
                          RuleController ruleController,
                          Player currentPlayer,
                          Player opponent,
                          int holeIndex) {
        super(board, ruleController, currentPlayer, opponent, holeIndex);
    }

    public RedMoveCommand(Board board,
                          RuleController ruleController,
                          Player currentPlayer,
                          Player opponent,
                          int holeIndex,
                          boolean silent) {
        super(board, ruleController, currentPlayer, opponent, holeIndex, silent);
    }

    @Override
    public boolean execute() {
        // 1. Check ownership
        if (!currentPlayer.ownsHoleIndex(holeIndex)) {
            System.out.println("Illegal move: that hole does not belong to you.");
            return false;
        }

        // 2. Take all red seeds from that hole
        Hole startHole = board.getHole(holeIndex);
        int seeds = startHole.takeAllRed();
        if (seeds <= 0) {
            System.out.println("Illegal move: no red seeds in that hole.");
            return false;
        }

        // 3. Sow using normal pattern
        SowStrategy strategy = new NormalSowStrategy();
        int lastIndex = strategy.sow(board, currentPlayer, holeIndex, seeds, SeedColor.RED);

        if (!silent) {
            System.out.printf("%s plays RED from hole %d (index %d), last seed lands in hole %d.%n",
                currentPlayer.getName(), holeIndex + 1, holeIndex, lastIndex + 1);
        }

        // 4. Apply rules
        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);

        return true;
    }
}

