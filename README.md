## Words Learning – MVP

Cette application Android livre le MVP complet de **Words Learning**, un outil de révision du vocabulaire Anglais ⇄ Français basé sur un système de répétition espacée (SRS) et des quiz à choix multiples.

Le code Kotlin/Compose suit les bonnes pratiques Android (architecture en couches, Room, DataStore, ViewModel + StateFlow) pour rester lisible et facilement extensible. L'application fonctionne immédiatement dans Android Studio : le premier lancement injecte automatiquement un corpus de 2000 mots regroupés par thèmes.

---

## Fonctionnalités principales

- **Tableau de bord de révision** : compteur des mots dus, sélection des thèmes et paramétrage du nombre d'options (5 à 10) par question.
- **Quiz adaptatif** : questions alternant traduction EN→FR et FR→EN, distracteurs thématiques, synthèse vocale (TTS) intégrée et feedback immédiat.
- **Bibliothèque filtrable** : consultation des mots par thème, exemples bilingues et statistiques SRS.
- **Ajout manuel de mots** : formulaire complet pour enrichir la base de vocabulaire.
- **Initialisation automatique** : ingestion unique des 2000 mots depuis `res/raw/initial_words.json` grâce à DataStore + Room.

---

## Architecture

| Couche | Rôle | Fichier clé |
| --- | --- | --- |
| UI (Jetpack Compose) | Écrans `Review`, `Quiz`, `Library`, `Add` orchestrés par Navigation Compose. | `app/src/main/java/com/example/myapplication/ui/screens/`
| ViewModel | Regroupe l'état UI (StateFlow), déclenche les actions utilisateur et expose l'API utilisée par les écrans. | `ui/WordsViewModel.kt`
| Repository | Logique métier : génération de quiz, progression SRS, accès Room. | `data/WordsRepository.kt`
| Base de données | Stockage des mots et des statistiques (Room). | `data/local/WordsDatabase.kt`
| Initialisation | Chargement des données seed (JSON → objets → Room). | `data/SeedWordLoader.kt`, `data/WordsInitializer.kt`

Chaque fichier inclut des commentaires KDoc décrivant son rôle pour faciliter la prise en main.

---

## Prérequis

1. **Android Studio Koala Feature Drop ou plus récent** avec le SDK Android 35 installé.
2. **JDK 17** (installé automatiquement avec Android Studio).
3. **Émulateur ou appareil** sous Android 8.0 (API 26) minimum.

---

## Installation et exécution

1. Cloner ce dépôt puis l'ouvrir dans Android Studio (`File > Open...`).
2. Laisser Android Studio synchroniser Gradle (les dépendances sont gérées via `gradle/libs.versions.toml`).
3. Créer ou sélectionner un périphérique virtuel (Pixel 6 / API 33 recommandé) ou brancher un appareil physique.
4. Cliquer sur **Run ▶** pour lancer l'application.
5. Au premier démarrage, patienter pendant l'initialisation de la base (quelques secondes maximum). Les mots, thèmes et statistiques se mettront ensuite à jour automatiquement.

### Lancement en ligne de commande

```bash
./gradlew assembleDebug
```

> ℹ️ Les tâches qui exécutent les tests unitaires/instrumentés nécessitent que la variable `ANDROID_HOME` ou le fichier `local.properties` pointe vers un SDK Android valide.

---

## Structure des données

- **`initial_words.json`** : contient 2000 enregistrements structurés (`english`, `french`, `theme`, `example`, `example_french`).
- Les mots ajoutés par l'utilisateur sont persistés dans `words_learning.db` via Room.
- Les intervalles SRS par défaut : 1 → 3 → 7 → 30 jours, avec ajustement automatique en fonction des bonnes/mauvaises réponses.

---

## Tests et qualité

- Architecture testable (Repository et ViewModel isolés).
- Linters Gradle/Compose disponibles (`./gradlew lint`, `./gradlew ktlint` si ajouté).
- Lancement des tests unitaires : `./gradlew testDebugUnitTest` (SDK requis).

---

## Personnalisation rapide

- **Nouveau thème** : modifier ou ajouter un champ `theme` dans `initial_words.json`.
- **Intervalles SRS** : adapter la liste `srsIntervalsDays` dans `WordsRepository`.
- **Nombre d'options par défaut** : mettre à jour `WordsViewModel.DEFAULT_OPTION_COUNT`.

---

## Licence

Projet livré dans le cadre du MVP Words Learning. Utilisation interne ou prototypage libre.
