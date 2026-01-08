# Rendu projet AI Games – GIACCHERO Baptiste, Minh Quan HOANG

## Contenu du dépôt

Ce dépôt contient :

- Le **code original** que nous avons conçu au départ sans connaître le format exact de la compétition (situé dans le dossier `code/`).
- L’**arbitre** `Arbitre.java` et le **joueur externe** `JoueurExterne.java` qui fonctionnent avec les classes autour de ces deux fichiers (moteur de jeu + IA).
- Le **rapport** du projet : `Rapport_Ai_games_Giacchero_Baptiste_Minh_Quan_HOANG.pdf`.

Le dépôt complet est accessible à l’adresse suivante :

- https://github.com/ba-giacchero/AI_games_Giacchero_Baptiste_Minh_Quan_HOANG

## Compilation

Depuis la racine du projet (dossier `Competitions`) :

```bash
javac -d bin code\src\models\*.java code\src\controllers\*.java Arbitre.java JoueurExterne.java
```

Cette commande :

- compile les classes du moteur de jeu (`code/src/models`) ;
- compile les contrôleurs et l’IA (`code/src/controllers`) ;
- compile `Arbitre.java` et `JoueurExterne.java` ;
- place tous les `.class` résultants dans le dossier `bin/`.

## Exécution

Toujours depuis la racine du projet :

```bash
java --enable-preview -cp bin Arbitre
```

Cette commande lance l’arbitre, qui démarre ensuite deux processus `JoueurExterne` jouant l’un contre l’autre avec la même IA. L’exécution peut également être lancée directement depuis VS Code via le bouton **Run** sur `Arbitre.java`, qui utilise une commande équivalente en arrière-plan.

## Remarques

- L’arbitre utilise les règles "Mancala 2025" fournies dans le fichier `Rules2025 (1).txt`.
- Le joueur externe implémente une IA Minimax avec évaluation par phases (profil `BALANCED`).
- Le dépôt contient à la fois le code d’origine (dans `code/`) et la version finale utilisée pour la compétition (`Arbitre.java`, `JoueurExterne.java` + classes associées).
