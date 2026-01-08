# Réglages et tests d'IA

Ce fichier sert à garder une trace des paramètres d'IA testés et de leurs résultats.

## Versions d'IA actuelles

Dans `src/Main.java`, deux IA minimax sont configurées :

- IA simple : profondeur `depthSimple = 3`, limite de temps `timeLimitMsSimple = 500` ms.
- IA forte : profondeur `depthStrong = 6`, limite de temps `timeLimitMsStrong = 1000` ms.

Les deux utilisent la même fonction d'évaluation définie dans `src/controllers/Evaluator.java`.

## Modes de jeu

Au lancement de `Main`, un menu permet de choisir :

1. Human vs AI  (Joueur 1 humain, Joueur 2 IA forte)
2. AI vs Human  (Joueur 1 IA forte, Joueur 2 humain)
3. AI vs AI     (Joueur 1 IA simple, Joueur 2 IA forte)
4. Human vs Human

Le mode 3 (AI vs AI) est pratique pour comparer les versions d'IA.

## Comment tester les IA entre elles

- Choisir le mode 3 (AI vs AI).
- Laisser se dérouler une partie complète et noter :
  - Qui gagne (IA simple ou IA forte)
  - Le nombre de graines capturées par chaque côté
  - La durée moyenne des coups (visuellement ou avec un chronomètre si besoin)
- Modifier ensuite les paramètres dans `Main` (profondeur, limites de temps) et relancer plusieurs parties.

Idée : 
- Faire une petite série de N parties (par exemple 10) pour chaque configuration et compter le nombre de victoires de chaque IA.

Dans `AITournament`, chaque partie IA vs IA est également limitée à **400 coups maximum** (200 par joueur). Si ce seuil est atteint sans condition de fin de partie standard, la partie est arrêtée et comptée comme nulle.

## Ajustement de la contrainte de temps

La classe `MinimaxPlayerController` accepte deux paramètres principaux :

- `maxDepth` : profondeur maximale de la recherche minimax.
- `timeLimitMillis` : limite de temps approximative par coup (en millisecondes).

Si `timeLimitMillis > 0`, chaque appel de l'IA coupe la recherche dès que la limite est dépassée et renvoie la meilleure information disponible.

Pour rester sous 1 seconde par coup, garder `timeLimitMillis` ≤ 1000 et ajuster `maxDepth` en fonction de la performance observée.

## Pistes d'amélioration futures

- Affiner encore la fonction d'évaluation : captures en chaîne plus longues, situations de famine gagnantes forcées, etc.
- Tester systématiquement plusieurs profils (Balanced, Base, AdvancedHeuristic, défensive) via `AITournament` et garder la meilleure contre des IA externes.
