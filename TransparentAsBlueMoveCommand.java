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
            return false;
        }
        Hole startHole = board.getHole(holeIndex);
        int tSeeds = startHole.takeAllTransparent();
        int bSeeds = startHole.takeAllBlue();
        if (tSeeds + bSeeds <= 0) {
            return false;
        }
        SowStrategy bluePattern = new OpponentOnlySowStrategy();
        int lastIndex = holeIndex;
        if (tSeeds > 0) {
            lastIndex = bluePattern.sow(board, currentPlayer, lastIndex, tSeeds, SeedColor.TRANSPARENT);
        }
        if (bSeeds > 0) {
            lastIndex = bluePattern.sow(board, currentPlayer, lastIndex, bSeeds, SeedColor.BLUE);
        }
        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);
        return true;
    }
}
