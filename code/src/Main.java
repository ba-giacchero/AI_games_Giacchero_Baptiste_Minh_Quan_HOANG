
import controllers.Evaluator;
import controllers.HumanPlayerController;
import controllers.MinimaxPlayerController;
import controllers.PhaseEvaluator;
import controllers.PlayerController;
import controllers.RuleController;
import java.util.Scanner;
import models.*;

public class Main {

    public static void main(String[] args) {
        Board board = new Board();
        Player player1 = new Player(0, "Player 1");
        Player player2 = new Player(1, "Player 2");
        Player[] players = { player1, player2 };

        RuleController ruleController = new RuleController();
        MoveFactory moveFactory = new MoveFactory(board, ruleController, players);

        Scanner scanner = new Scanner(System.in);

        // Choix du mode et configuration des joueurs (humain / IA).
        System.out.println("Select mode:");
        System.out.println("  1) Human vs AI");
        System.out.println("  2) AI vs Human");
        System.out.println("  3) AI vs AI");
        System.out.println("  4) Human vs Human");
        System.out.print("Mode (1-4, default=1): ");
        String modeLine = scanner.nextLine();
        int mode;
        try {
            mode = Integer.parseInt(modeLine.trim());
        } catch (NumberFormatException e) {
            mode = 1;
        }
        if (mode < 1 || mode > 4) {
            mode = 1;
        }

        // On peut ajuster ici la profondeur max et la limite de temps (en ms)
        int depthSimple = 3;
        int depthStrong = 6;
        long timeLimitMsSimple = 500;  // 0.5s
        long timeLimitMsStrong = 1000; // 1s

        // IA simple : heuristique "Evaluator" standard
        Evaluator evaluatorSimple = new Evaluator();
        // IA forte (par défaut pour l'évaluation) : heuristique par phases "BALANCED" (PhaseBalanced)
        Evaluator evaluatorStrong = new PhaseEvaluator(PhaseEvaluator.PhaseProfile.BALANCED);

        PlayerController human = new HumanPlayerController(scanner);
        PlayerController aiSimple = new MinimaxPlayerController(evaluatorSimple, depthSimple, timeLimitMsSimple);
        PlayerController aiStrong = new MinimaxPlayerController(evaluatorStrong, depthStrong, timeLimitMsStrong);

        PlayerController[] controllers = new PlayerController[2];
        switch (mode) {
            case 1 -> { // Human vs AI (IA côté joueur 2)
                controllers[0] = human;
                controllers[1] = aiStrong;
            }
            case 2 -> { // AI vs Human (IA côté joueur 1)
                controllers[0] = aiStrong;
                controllers[1] = human;
            }
            case 3 -> { // AI vs AI (pour tester deux versions différentes)
                controllers[0] = aiSimple;  // IA de base
                controllers[1] = aiStrong;  // IA plus forte
            }
            case 4 -> { // Human vs Human
                controllers[0] = human;
                controllers[1] = human;
            }
            default -> {
                controllers[0] = human;
                controllers[1] = aiStrong;
            }
        }

        System.out.println("Mancala-like Game (full 2-player loop)");
        System.out.println("Moves: N R, N B, N TR, N TB   (e.g. 5R, 8B, 4TR, 7TB)");
        System.out.println("Player 1 owns odd holes (1,3,5,...,15)");
        System.out.println("Player 2 owns even holes (2,4,6,...,16)");
        System.out.println();

        int currentPlayerIndex = 0; // start with Player 1

        // -------- main game loop --------
        while (true) {
            Player current = players[currentPlayerIndex];

            // show board & scores
            System.out.println();
            System.out.println("====================================");
            System.out.printf("Captured: P1=%d  P2=%d%n",
                    player1.getCaptured(), player2.getCaptured());
            System.out.printf("Current player: %s (%s holes)%n",
                    current.getName(),
                    current.getIndex() == 0 ? "odd" : "even");
            board.print();

            // check for game over
            if (ruleController.isGameOver(board, players)) {
                System.out.println("Game over!");
                int w = ruleController.winner(players);
                if (w == -1) {
                    System.out.println("Result: Draw.");
                } else {
                    System.out.printf("Winner: %s%n", players[w].getName());
                }
                break;
            }

            // ask controller (human or AI) for a move
            System.out.printf("%s, enter your move (e.g. 5R, 8B, 4TR, 7TB):%n", current.getName());
            System.out.print("> ");

            PlayerController controller = controllers[currentPlayerIndex];
            String moveText = controller.chooseMove(board, players, currentPlayerIndex);

            if (moveText == null) {
                System.out.println("No move provided. Exiting game.");
                break;
            }

            moveText = moveText.trim();
            if (moveText.isEmpty()) {
                System.out.println("Empty move. Please type something.");
                continue;
            }

            MoveCommand command = moveFactory.createMove(moveText, current);
            if (command == null) {
                // invalid syntax or unknown type
                System.out.println("Could not create a move from your input. Try again.");
                continue; // same player tries again
            }

            boolean ok = command.execute();
            if (!ok) {
                System.out.println("Move was illegal and not applied. Try again.");
                continue; // same player tries again
            }

            // move succeeded -> switch player
            currentPlayerIndex = 1 - currentPlayerIndex;
        }

        System.out.println();
        System.out.println("Final board:");
        board.print();
        System.out.printf("Final captured: P1=%d  P2=%d%n",
                player1.getCaptured(), player2.getCaptured());
    }
}

