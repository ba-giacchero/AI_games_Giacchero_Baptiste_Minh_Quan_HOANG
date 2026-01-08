public abstract class AbstractMoveCommand implements MoveCommand {
    protected final Board board;
    protected final RuleController ruleController;
    protected final Player currentPlayer;
    protected final Player opponent;
    protected final int holeIndex;
    protected final boolean silent;

    protected AbstractMoveCommand(Board board,
                                  RuleController ruleController,
                                  Player currentPlayer,
                                  Player opponent,
                                  int holeIndex) {
        this(board, ruleController, currentPlayer, opponent, holeIndex, false);
    }

    protected AbstractMoveCommand(Board board,
                                  RuleController ruleController,
                                  Player currentPlayer,
                                  Player opponent,
                                  int holeIndex,
                                  boolean silent) {
        this.board = board;
        this.ruleController = ruleController;
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;
        this.holeIndex = holeIndex;
        this.silent = silent;
    }
}
