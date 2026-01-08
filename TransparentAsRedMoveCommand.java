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
        if (!currentPlayer.ownsHoleIndex(holeIndex)) {
            return false;
        }
        Hole startHole = board.getHole(holeIndex);
        int tSeeds = startHole.takeAllTransparent();
        int rSeeds = startHole.takeAllRed();
        if (tSeeds + rSeeds <= 0) {
            return false;
        }
        SowStrategy redPattern = new NormalSowStrategy();
        int lastIndex = holeIndex;
        if (tSeeds > 0) {
            lastIndex = redPattern.sow(board, currentPlayer, lastIndex, tSeeds, SeedColor.TRANSPARENT);
        }
        if (rSeeds > 0) {
            lastIndex = redPattern.sow(board, currentPlayer, lastIndex, rSeeds, SeedColor.RED);
        }
        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);
        return true;
    }
}
