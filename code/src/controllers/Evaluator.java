package controllers;

import models.Board;
import models.Hole;
import models.Player;

// Evaluates the game state for a given player based on the current board configuration.
// Returns a score in [0, 100]:
//   0   = position très mauvaise pour le joueur id_idx
//   100 = position très bonne pour le joueur id_idx
public class Evaluator {

    // Poids pour l'heuristique
    private final double capturedWeight;   // poids des graines capturées
    private final double boardWeight;      // poids des graines encore sur le côté du joueur
    private final double scale;            // facteur d'échelle pour normaliser vers [0,100]

    // Terme positionnel supplémentaire
    private final double famineWeight;     // avantage à avoir plus de graines côté joueur (pression de famine)

    // Poids fixe pour la mobilité (nombre approximatif de coups possibles)
    private static final double MOBILITY_WEIGHT = 0.2;

    // Constructeur par défaut : comportement initial + terme de famine modéré
    public Evaluator() {
        this(1.0, 0.5, 0.5, 0.3);
    }

    // Constructeur paramétrable simple
    public Evaluator(double capturedWeight, double boardWeight, double scale) {
        this(capturedWeight, boardWeight, scale, 0.3);
    }

    // Constructeur complet
    public Evaluator(double capturedWeight,
                     double boardWeight,
                     double scale,
                     double famineWeight) {
        this.capturedWeight = capturedWeight;
        this.boardWeight = boardWeight;
        this.scale = scale;
        this.famineWeight = famineWeight;
    }

    public int evaluate(Board board, Player[] players, int id_idx) {
        Player me = players[id_idx];
        Player opp = players[1 - id_idx];

        // 1. Cas terminaux : partie finie -> valeur extrême
        // Remarque : la détection de fin de partie est déjà faite dans la boucle principale
        // de jeu. Ici, on se contente d'une heuristique matérielle continue, ce qui évite
        // de dupliquer la logique de RuleController.

        // 2. Heuristique matérielle :
        //    - graines capturées comptent davantage
        //    - graines sur le côté du joueur comptent aussi mais un peu moins
        int myCaptured = me.getCaptured();
        int oppCaptured = opp.getCaptured();

        int myBoardSeeds = board.totalSeedsOwnedBy(me);
        int oppBoardSeeds = board.totalSeedsOwnedBy(opp);

        double myMaterial = capturedWeight * myCaptured + boardWeight * myBoardSeeds;
        double oppMaterial = capturedWeight * oppCaptured + boardWeight * oppBoardSeeds;

        double diff = myMaterial - oppMaterial; // positif si avantage pour "me"

        // 3. Heuristique positionnelle : avantage de famine
        // (avoir plus de graines de notre côté que l'adversaire).
        double famineDiff = myBoardSeeds - oppBoardSeeds;

        diff += famineWeight * famineDiff;

        // 4. Mobilité approximative : nombre de coups possibles pour chacun
        int myMobility = approximateMoveCount(board, me);
        int oppMobility = approximateMoveCount(board, opp);
        double mobilityDiff = myMobility - oppMobility;
        diff += MOBILITY_WEIGHT * mobilityDiff;

        // 5. Normalisation autour de 50, puis clamp dans [0,100]
        double rawScore = 50.0 + scale * diff;
        if (rawScore < 0.0) rawScore = 0.0;
        if (rawScore > 100.0) rawScore = 100.0;
        return (int) Math.round(rawScore);
    }

    // Approximation du nombre de coups légaux possibles pour un joueur,
    // en se basant uniquement sur la présence de graines dans ses trous.
    private int approximateMoveCount(Board board, Player player) {
        int count = 0;
        int numHoles = board.getNumHoles();
        for (int i = 0; i < numHoles; i++) {
            if (!player.ownsHoleIndex(i)) {
                continue;
            }
            Hole h = board.getHole(i);
            int red = h.getRed();
            int blue = h.getBlue();
            int transparent = h.getTransparent();

            if (red > 0) {
                count++;
            }
            if (blue > 0) {
                count++;
            }
            if (transparent + red > 0) {
                count++;
            }
            if (transparent + blue > 0) {
                count++;
            }
        }
        return count;
    }
}
