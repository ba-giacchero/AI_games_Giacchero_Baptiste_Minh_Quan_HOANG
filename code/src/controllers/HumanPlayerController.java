package controllers;

import java.util.Scanner;
import models.Board;
import models.Player;

// Simple human controller: reads moves from standard input.
public class HumanPlayerController implements PlayerController {

    private final Scanner scanner;

    public HumanPlayerController(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String chooseMove(Board board, Player[] players, int currentPlayerIndex) {
        String line = scanner.nextLine();
        if (line == null) {
            return null;
        }
        return line.trim();
    }
}
