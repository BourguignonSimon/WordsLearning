1. Clarification du Besoin (Contraintes du MVP)
Le cœur du MVP se concentre sur une seule paire de langues (Anglais $\leftrightarrow$ Français) et une seule source de données principale (les 2000 mots les plus courants).

Composant	MVP Requis	Impact sur le Projet
Paire de Langues	Anglais (LE) $\leftrightarrow$ Français (LM)	La fonctionnalité de gestion multilingue peut être simplifiée (ou reportée) pour la Phase 1.
Données Initiales	2000 Mots les Plus Courants en Anglais	Nécessite de trouver/compiler une liste de haute qualité (Mot + Traduction + Thème + Phrase Type, si possible) et de l'intégrer en dur (fichier JSON/CSV) dans l'application.
Organisation	Organisation par Thématique (Ex: Nourriture, Voyage, Maison)	Le modèle de données doit inclure un champ Thème pour chaque mot, permettant le filtrage et la création de sets de révision spécifiques.
F1 : Intégration	Fonctionnalité d'Importation Reportée	L'accent peut être mis sur l'importation automatique au premier lancement (les 2000 mots) et la saisie manuelle. Les formats JSON/XML/Excel ne sont pas la priorité du MVP.
F2 & F3 : Quizz QCM	Fonctionnel (5-10 options)	Logique de génération de distracteurs assurée par le pool des 2000 mots, organisés par thème (pour des distracteurs thématiquement proches).
2. Identification des Informations Supplémentaires (Focus MVP)
Le focus est désormais sur l'acquisition et la structuration des données et l'implémentation du SRS.

Domaine	Question(s) Clé(s)	Impact sur le Projet
Acquisition des 2000 Mots	Avez-vous déjà une liste structurée (CSV/JSON) de ces 2000 mots avec traduction, thématique et phrases types ?	Si non, une étape de recherche de données est nécessaire, potentiellement via Google Search.
Définition de "Thématique"	Quelles sont les catégories thématiques souhaitées (Ex: A1, B2, 50 thèmes précis) ?	Détermine la structure du champ Set/Thème dans la base de données.
MVP Simplification F1	Faut-il complètement retirer l'importation de fichiers (JSON/XML/Excel) du MVP pour se concentrer uniquement sur les 2000 mots et la saisie manuelle ?	Simplifie massivement la phase de développement initiale. (Recommandé)
Prononciation (TTS)	La qualité de la voix TTS native d'Android est-elle suffisante, ou doit-on envisager une API externe (plus coûteuse) pour une meilleure qualité ?	Le TTS natif est idéal pour le MVP.
3. Définition Complète du Projet (Version MVP Simple)
Objectif du MVP
Développer la version minimale de l'application capable de démontrer la valeur des quiz QCM et du SRS, en utilisant le set de 2000 mots anglais/français comme unique source de contenu initial.

Fonctionnalités Techniques Clés du MVP
Catégorie	Fonctionnalité	Description Technique
Contenu	Initialisation BD	Au premier lancement, l'application charge les 2000 mots (avec Traduction, Thème) à partir d'un fichier de données interne.
Quizz	Quizz Anglais $\leftrightarrow$ Français	Implémentation des QCM (F2 et F3) avec 5 à 10 options. La base de distracteurs est limitée aux 2000 mots.
Logique SRS	Fonctionnel Basique	Le SRS doit enregistrer le succès/échec de l'utilisateur et ajuster la date de la Prochaine Révision selon un intervalle de base (par exemple : 1 jour $\rightarrow$ 3 jours $\rightarrow$ 7 jours $\rightarrow$ 30 jours).
UX/UI	Filtrage Thématique	L'utilisateur doit pouvoir sélectionner un ou plusieurs thèmes parmi les 2000 mots pour commencer une session de révision.
Core Android	TTS	Utilisation de l'API Android Text-to-Speech pour la lecture des mots anglais.
Gestion des Données (MVP)	Saisie Manuelle	L'utilisateur peut ajouter de nouveaux mots manuellement (un à un) s'il le souhaite. L'importation de fichiers structurés est exclue du MVP.
