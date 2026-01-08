import controllers.MinimaxPlayerController;
import controllers.PhaseEvaluator;
import controllers.RuleController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import models.Board;
import models.MoveCommand;
import models.MoveFactory;
import models.Player;

public class JoueurExterne {
    private final String name;
    private final int playerIndex; // 0 pour premier joueur (trous impairs), 1 pour second (trous pairs)

    private final Board board;
    private final Player[] players;
    private final RuleController ruleController;
    private final MoveFactory moveFactory;
    private final MinimaxPlayerController ai;

    JoueurExterne(String name) {
        this.name = name;
        // Déduction simple de l'index du joueur à partir du nom passé par l'arbitre
        // JoueurA -> index 0, JoueurB -> index 1, défaut = 0
        int idx;
        String lower = name.toLowerCase();
        if (lower.endsWith("b")) {
            idx = 1;
        } else {
            idx = 0;
        }
        this.playerIndex = idx;

        // Initialisation de l'état de jeu local (identique pour les deux joueurs)
        this.board = new Board();
        Player p1 = new Player(0, "Player 1");
        Player p2 = new Player(1, "Player 2");
        this.players = new Player[] { p1, p2 };

        this.ruleController = new RuleController();
        // MoveFactory en mode silencieux pour ne pas polluer stdout :
        // le joueur externe ne doit imprimer que le coup (ex: "13B").
        this.moveFactory = new MoveFactory(board, ruleController, players, true);

        // IA Minimax avec évaluateur par phases BALANCED
        int depthStrong = 6;
        long timeLimitMsStrong = 1000L; // 1 seconde max par coup (l'arbitre a 3s de timeout)
        this.ai = new MinimaxPlayerController(
            new PhaseEvaluator(PhaseEvaluator.PhaseProfile.BALANCED),
            depthStrong,
            timeLimitMsStrong
        );
    }

    private int opponentIndex() {
        return 1 - playerIndex;
    }

    private boolean applyMoveForPlayer(String moveText, int idx) {
        MoveCommand cmd = moveFactory.createMove(moveText, players[idx]);
        if (cmd == null) {
            return false;
        }
        return cmd.execute();
    }

    private String computeBestMove() {
        return ai.chooseMove(board, players, playerIndex);
    }

    public static void main(String[] args) throws Exception {
        String playerName = (args.length > 0) ? args[0] : "Joueur";
        JoueurExterne joueur = new JoueurExterne(playerName);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String etat;

        while ((etat = in.readLine()) != null) {
            // ATTENTION : NE PAS ÉCRIRE D'AUTRES TRACES SUR STDOUT QUE LE COUP
            etat = etat.trim();
            if (etat.isEmpty()) {
                continue;
            }

            String coup;

            if ("START".equalsIgnoreCase(etat)) {
                // Premier coup de la partie pour ce joueur
                coup = joueur.computeBestMove();
                if (coup == null) {
                    // Aucun coup trouvé : on envoie un texte vide (l'arbitre gérera)
                    coup = "";
                } else {
                    // On applique localement notre propre coup
                    joueur.applyMoveForPlayer(coup, joueur.playerIndex);
                }
            } else if ("END".equalsIgnoreCase(etat)) {
                // Optionnel : si un arbitre décidait d'envoyer END, on termine proprement
                break;
            } else {
                // etat contient le dernier coup joué (par l'adversaire)
                boolean ok = joueur.applyMoveForPlayer(etat, joueur.opponentIndex());
                if (!ok) {
                    // Mouvement adverse invalide du point de vue local : on répond un coup quelconque
                    coup = joueur.computeBestMove();
                    if (coup == null) {
                        coup = "";
                    } else {
                        joueur.applyMoveForPlayer(coup, joueur.playerIndex);
                    }
                } else {
                    // Après avoir intégré le coup adverse, on joue le nôtre
                    coup = joueur.computeBestMove();
                    if (coup == null) {
                        coup = "";
                    } else {
                        joueur.applyMoveForPlayer(coup, joueur.playerIndex);
                    }
                }
            }

            System.out.println(coup);
            System.out.flush();
        }
    }
}
