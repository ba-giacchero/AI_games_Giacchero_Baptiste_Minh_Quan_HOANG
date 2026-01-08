package controllers;

import models.Board;
import models.Player;

/**
 * Evaluateur qui adapte son comportement en fonction de la phase de jeu
 * (ouverture, milieu, fin) en choisissant différents ensembles de poids.
 */
public class PhaseEvaluator extends Evaluator {

    private final Evaluator openingEval;
    private final Evaluator midEval;
    private final Evaluator endEval;

    // Seuils sur le nombre total de graines pour changer de phase.
    // 96 graines au départ, fin de partie quand < 10.
    private final int openingThreshold; // au-dessus => ouverture
    private final int midThreshold;     // au-dessus => milieu, en dessous => fin

    public enum PhaseProfile {
        BALANCED,
        AGGRESSIVE_ENDGAME,
        SAFE_OPENING,
        ULTIMATE
    }

    public PhaseEvaluator() {
        this(PhaseProfile.BALANCED);
    }

    public PhaseEvaluator(PhaseProfile profile) {
        // On ignore les paramètres du parent et on utilise des Evaluator internes.
        super(1.0, 0.7, 0.5, 0.3);

        switch (profile) {
            case BALANCED -> {
            // Profil actuel : ouverture prudente, milieu équilibré, fin un peu agressive.
            this.openingEval = new Evaluator(
                0.8,
                0.9,
                0.5,
                0.3
            );
            this.midEval = new Evaluator(
                1.0,
                0.7,
                0.5,
                0.3
            );
            this.endEval = new Evaluator(
                1.3,
                0.5,
                0.5,
                0.3
            );
            this.openingThreshold = 70;
            this.midThreshold = 40;
            }
            case AGGRESSIVE_ENDGAME -> {
            // Ouverture proche de Balanced, milieu similaire,
            // fin de partie très orientée captures.
            this.openingEval = new Evaluator(
                0.9,
                0.8,
                0.5,
                0.3
            );
            this.midEval = new Evaluator(
                1.1,
                0.7,
                0.5,
                0.3
            );
            this.endEval = new Evaluator(
                1.6,
                0.4,
                0.5,
                0.3
            );
            this.openingThreshold = 70;
            this.midThreshold = 45; // on élargit un peu le milieu
            }
            case SAFE_OPENING -> {
            // Ouverture très prudente (on évite les gros cadeaux),
            // milieu équilibré, fin modérément agressive.
            this.openingEval = new Evaluator(
                0.7,
                1.0,
                0.5,
                0.25
            );
            this.midEval = new Evaluator(
                1.0,
                0.7,
                0.5,
                0.3
            );
            this.endEval = new Evaluator(
                1.4,
                0.5,
                0.5,
                0.3
            );
            this.openingThreshold = 75; // ouverture un peu plus longue
            this.midThreshold = 40;
            }
            case ULTIMATE -> {
            // Profil "polyvalent" :
            //  - ouverture très prudente (comme SAFE_OPENING),
            //  - milieu inspiré d'AdvancedHeuristic,
            //  - fin de partie très agressive (comme AGGRESSIVE_ENDGAME).
            this.openingEval = new Evaluator(
                0.7,
                1.0,
                0.5,
                0.25
            );
            this.midEval = new Evaluator(
                1.2,
                0.6,
                0.5,
                0.35
            );
            this.endEval = new Evaluator(
                1.6,
                0.4,
                0.5,
                0.3
            );
            this.openingThreshold = 75;
            this.midThreshold = 45;
            }
            default -> throw new IllegalArgumentException("Unknown phase profile: " + profile);
        }
        }

    @Override
    public int evaluate(Board board, Player[] players, int id_idx) {
        int total = board.totalSeeds();
        if (total > openingThreshold) {
            return openingEval.evaluate(board, players, id_idx);
        } else if (total > midThreshold) {
            return midEval.evaluate(board, players, id_idx);
        } else {
            return endEval.evaluate(board, players, id_idx);
        }
    }
}
