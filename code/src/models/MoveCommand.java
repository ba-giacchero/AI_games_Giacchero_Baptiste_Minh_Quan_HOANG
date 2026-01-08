package models;

public interface MoveCommand {
    /**
     * Execute the move.
     * @return true if move was legal and applied, false otherwise
     */
    boolean execute();
}
