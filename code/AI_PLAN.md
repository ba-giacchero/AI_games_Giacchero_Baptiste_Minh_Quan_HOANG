# Plan IA Awalé variant

## Objectifs
- Implémenter une IA basée sur minimax (avec profondeur limitée) pour la variante d'Awalé décrite dans `reminders/Rules.txt`.
- Utiliser une fonction d'évaluation qui renvoie une valeur entre 0 et 100.
  - 0 = très mauvais pour le joueur considéré, 100 = très bon.
- Dans un premier temps : version simple mais correcte.
- Ensuite : améliorer pour battre des IA "standard" (probablement générées par d'autres assistants).

## Étapes
1. Comprendre moteur de jeu existant (Board, MoveCommand, RuleController, etc.).
2. Implémenter `Evaluator.evaluate(Board board, Player[] players, int id_idx)` avec une heuristique matérielle (graines capturées + graines sur le côté de chaque joueur), normalisée sur [0, 100].
3. Créer un contrôleur `MinimaxPlayerController` qui implémente `PlayerController` et utilise minimax pour choisir un coup.
4. Créer un contrôleur `HumanPlayerController` qui lit les coups au clavier.
5. Adapter `Main` pour utiliser des `PlayerController` (par exemple : Joueur 1 = IA, Joueur 2 = humain).
6. Tester la boucle complète, ajuster profondeur de recherche et paramètres de l'évaluation.
7. Version améliorée :
   - Ajouter alpha-bêta pour explorer plus profondément.
   - Raffiner la fonction d'évaluation (menaces de capture, trous à 2/3 graines, situations de famine, etc.).

## Notes
- On clone les états (`Board`, `Player[]`) pour simuler des coups dans minimax sans casser la vraie partie.
- On réutilise les commandes de coup (`RedMoveCommand`, `BlueMoveCommand`, etc.) pour appliquer les règles fidèlement.
- On fait toujours l'évaluation du point de vue d'un joueur racine (l'IA) et minimax alterne max/min selon le joueur courant.
 - Règle pratique de tournoi : une partie ne doit pas dépasser 400 coups au total (soit 200 coups par joueur / IA).
