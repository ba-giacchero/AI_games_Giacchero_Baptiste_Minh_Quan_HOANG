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
        if (!currentPlayer.ownsHoleIndex(holeIndex)) {
            return false;
        }
        Hole startHole = board.getHole(holeIndex);
        int seeds = startHole.takeAllRed();
        if (seeds <= 0) {
            return false;
        }
        SowStrategy strategy = new NormalSowStrategy();
        int lastIndex = strategy.sow(board, currentPlayer, holeIndex, seeds, SeedColor.RED);
        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);
        return true;
    }
}
