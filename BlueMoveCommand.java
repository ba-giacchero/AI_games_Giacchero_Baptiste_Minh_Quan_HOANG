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
            return false;
        }
        Hole startHole = board.getHole(holeIndex);
        int seeds = startHole.takeAllBlue();
        if (seeds <= 0) {
            return false;
        }
        SowStrategy strategy = new OpponentOnlySowStrategy();
        int lastIndex = strategy.sow(board, currentPlayer, holeIndex, seeds, SeedColor.BLUE);
        ruleController.applyCaptures(board, currentPlayer, lastIndex);
        ruleController.applyStarvation(board, currentPlayer, opponent);
        return true;
    }
}
