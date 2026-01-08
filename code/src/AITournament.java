import controllers.Evaluator;
import controllers.MinimaxPlayerController;
import controllers.PhaseEvaluator;
import controllers.PlayerController;
import controllers.RuleController;
import java.util.Random;
import models.Board;
import models.MoveCommand;
import models.MoveFactory;
import models.Player;

/**
 * Petit programme pour jouer plusieurs parties IA vs IA
 * et comparer deux configurations d'IA.
 */
public class AITournament {

    private record AIConfig(
            String name,
            int depth,
            long timeLimitMs,
            double capturedWeight,
            double boardWeight,
            double scale,
            double famineWeight,
            boolean phased
    ) {}

    public static void main(String[] args) {
    // Variantes d'IA à tester :
    AIConfig cfgBalanced = new AIConfig(
        "Balanced",          // nom
        5,                    // profondeur
        800,                  // temps max (ms)
        1.0, 0.7, 0.5,        // capturedWeight, boardWeight, scale
        0.3,                  // famineWeight
        false                 // phased
    );

    // Variante qui valorise un peu plus les captures (surtout utile en fin de partie)
    AIConfig cfgBalancedCapture = new AIConfig(
        "BalancedCapture",   // nom
        5,
        800,
        1.2, 0.6, 0.5,
        0.3,
        false
    );

    // Variante qui insiste davantage sur la famine (plus de graines de notre côté)
    AIConfig cfgBalancedFamine = new AIConfig(
        "BalancedFamine",    // nom
        5,
        800,
        1.0, 0.7, 0.5,
        0.5,                // famine un peu plus lourd
        false
    );

    // Variante "Advanced" avec pondération proche de BalancedCapture
    AIConfig cfgAdvanced = new AIConfig(
        "AdvancedHeuristic", // nom
        5,
        800,
        1.2, 0.6, 0.5,
        0.35,
        false
        );

        // IA phase-aware, qui change de comportement en fonction du nombre
        // de graines restantes (ouverture / milieu / fin de partie).
        AIConfig cfgPhaseBalanced = new AIConfig(
            "PhaseBalanced",
            5,
            800,
            1.0, 0.7, 0.5,
            0.3,
            true
        );

        // Variante phase plus agressive en fin de partie
        AIConfig cfgPhaseAggressive = new AIConfig(
            "PhaseAggressive",
            5,
            800,
            1.0, 0.7, 0.5,
            0.3,
            true
        );

        // Variante phase avec ouverture plus prudente
        AIConfig cfgPhaseSafeOpening = new AIConfig(
            "PhaseSafeOpening",
            5,
            800,
            1.0, 0.7, 0.5,
            0.3,
            true
        );

        // Variante phase "ultime" polyvalente (safe opening + advanced + fin agressive)
        AIConfig cfgPhaseUltimate = new AIConfig(
            "PhaseUltimate",
            5,
            800,
            1.0, 0.7, 0.5,
            0.3,
            true
        );

    // Choisir ici quelles IA s'affrontent dans le tournoi :
    // Tu peux modifier librement cfgA et cfgB pour tester n'importe quelle paire.
    AIConfig cfgA = cfgAdvanced;
    AIConfig cfgB = cfgPhaseBalanced;

        int games = 20; // nombre de parties du tournoi
        runTournament(cfgA, cfgB, games);
    }

    private static void runTournament(AIConfig cfgA, AIConfig cfgB, int games) {
        int winsA = 0;
        int winsB = 0;
        int draws = 0;

        Random rng = new Random(1L);

        for (int g = 0; g < games; g++) {
            // On alterne qui commence pour être plus juste
            boolean aStarts = (g % 2 == 0);

            int result = playSingleGame(cfgA, cfgB, aStarts, rng);
            if (result == 0) {
                winsA++;
            } else if (result == 1) {
                winsB++;
            } else {
                draws++;
            }

            System.out.printf("Game %d result: %s%n", g + 1,
                    result == 0 ? cfgA.name() : (result == 1 ? cfgB.name() : "Draw"));
        }

        System.out.println("====================================");
        System.out.println("Tournament summary:");
        System.out.printf("%s wins: %d%n", cfgA.name(), winsA);
        System.out.printf("%s wins: %d%n", cfgB.name(), winsB);
        System.out.printf("Draws: %d%n", draws);
    }

    /**
     * Joue une partie IA vs IA avec deux configurations données.
     * @return 0 si cfgA gagne, 1 si cfgB gagne, -1 si nul.
     */
    private static int playSingleGame(AIConfig cfgA, AIConfig cfgB, boolean aStarts, Random rng) {
        Board board = new Board();
        Player player1 = new Player(0, "Player 1");
        Player player2 = new Player(1, "Player 2");
        Player[] players = { player1, player2 };

        RuleController ruleController = new RuleController();
        MoveFactory moveFactory = new MoveFactory(board, ruleController, players);

        // Construire les deux IA avec leurs évaluateurs propres
        Evaluator evalA = createEvaluator(cfgA);
        Evaluator evalB = createEvaluator(cfgB);

        PlayerController aiA = new MinimaxPlayerController(evalA, cfgA.depth(), cfgA.timeLimitMs());
        PlayerController aiB = new MinimaxPlayerController(evalB, cfgB.depth(), cfgB.timeLimitMs());

        PlayerController[] controllers = new PlayerController[2];
        if (aStarts) {
            controllers[0] = aiA;
            controllers[1] = aiB;
        } else {
            controllers[0] = aiB;
            controllers[1] = aiA;
        }

        int currentPlayerIndex = 0;
        int maxMoves = 400; // garde-fou : 400 coups max (200 par joueur)
        int movesPlayed = 0;

        while (true) {
            if (ruleController.isGameOver(board, players)) {
                break;
            }

            if (movesPlayed++ > maxMoves) {
                // partie anormalement longue : on arrête et on considère nul
                break;
            }

            Player current = players[currentPlayerIndex];
            PlayerController controller = controllers[currentPlayerIndex];

            String moveText = controller.chooseMove(board, players, currentPlayerIndex);
            if (moveText == null) {
                // Si une IA ne donne pas de coup, on considère que l'autre gagne
                return aStarts ^ (currentPlayerIndex == 0) ? 0 : 1;
            }

            moveText = moveText.trim();
            if (moveText.isEmpty()) {
                return aStarts ^ (currentPlayerIndex == 0) ? 0 : 1;
            }

            MoveCommand command = moveFactory.createMove(moveText, current);
            if (command == null) {
                // coup invalide -> défaite
                return aStarts ^ (currentPlayerIndex == 0) ? 0 : 1;
            }

            boolean ok = command.execute();
            if (!ok) {
                // coup illégal -> défaite
                return aStarts ^ (currentPlayerIndex == 0) ? 0 : 1;
            }

            currentPlayerIndex = 1 - currentPlayerIndex;
        }

        int w = ruleController.winner(players);
        if (w == -1) {
            return -1; // nul
        }

        // w est 0 ou 1 en termes de Player[]
        // On doit le traduire en "quel config a gagné" en fonction de qui a commencé.
        if (aStarts) {
            return (w == 0) ? 0 : 1;
        } else {
            return (w == 0) ? 1 : 0;
        }
    }

    // Crée le bon évaluateur en fonction de la configuration (statique ou "phased").
    private static Evaluator createEvaluator(AIConfig cfg) {
        if (cfg.phased()) {
            // IA phased : on choisit le profil en fonction du nom de la config.
            return switch (cfg.name()) {
                case "PhaseAggressive" -> new PhaseEvaluator(PhaseEvaluator.PhaseProfile.AGGRESSIVE_ENDGAME);
                case "PhaseSafeOpening" -> new PhaseEvaluator(PhaseEvaluator.PhaseProfile.SAFE_OPENING);
                case "PhaseUltimate" -> new PhaseEvaluator(PhaseEvaluator.PhaseProfile.ULTIMATE);
                default -> new PhaseEvaluator(PhaseEvaluator.PhaseProfile.BALANCED);
            };
        }
        return new Evaluator(cfg.capturedWeight(), cfg.boardWeight(), cfg.scale(), cfg.famineWeight());
    }
}
